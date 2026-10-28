package com.example.network

import com.example.model.GameModel
import com.example.model.RouteComparison
import com.example.model.RouteMetrics
import com.example.model.ServerNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

/**
 * Compara sondagens reais sem confundir o RTT até ao relay com o RTT até ao jogo.
 * Um nó rápido até ao telemóvel não prova que a rota completa até ao servidor do jogo
 * seja mais rápida; essa confirmação exige sondagens end-to-end através do relay.
 */
object RouteOptimizerEngine {
    suspend fun benchmarkGameRoute(
        game: GameModel,
        selectedServer: ServerNode?,
        candidateServers: List<ServerNode>
    ): Pair<RouteComparison, ServerNode> = withContext(Dispatchers.IO) {
        val targetHost = game.targetHosts.firstOrNull()
            ?: throw IllegalStateException("Este jogo não tem um endereço de teste configurado.")

        val directDeferred = async {
            NetworkLatencyProbe.measureHost(targetHost, game.defaultPort, samples = 5)
        }

        val serverToUse = selectedServer ?: run {
            val available = candidateServers.filter { it.isOnline }
            if (available.isEmpty()) {
                throw IllegalStateException("Não há servidores RouteBoost configurados. Adiciona um servidor relay real primeiro.")
            }
            val tested = available.take(5).map { server ->
                async {
                    val metrics = NetworkLatencyProbe.measureHost(server.host, server.port, samples = 3)
                    server.copy(currentPingMs = metrics.pingMs, jitterMs = metrics.jitterMs)
                }
            }.awaitAll()
            tested.filter { (it.currentPingMs ?: -1) > 0 }
                .minByOrNull { it.currentPingMs ?: Int.MAX_VALUE }
                ?: throw IllegalStateException("Nenhum servidor RouteBoost respondeu ao teste.")
        }

        val directMetrics = directDeferred.await()
        val nodeMetrics = NetworkLatencyProbe.measureHost(serverToUse.host, serverToUse.port, samples = 5)
        val comparison = compareMetrics(directMetrics, nodeMetrics, serverToUse.name, game.name)
        Pair(comparison, serverToUse.copy(currentPingMs = nodeMetrics.pingMs, jitterMs = nodeMetrics.jitterMs))
    }

    fun compareMetrics(direct: RouteMetrics, node: RouteMetrics, serverName: String, gameName: String): RouteComparison {
        val directValid = direct.pingMs > 0
        val nodeValid = node.pingMs > 0
        val message = when {
            !directValid && !nodeValid -> "Não foi possível medir a ligação a $gameName nem ao nó $serverName."
            !directValid -> "A ligação direta a $gameName não respondeu. O nó respondeu, mas isso não prova que a rota pelo relay funcione."
            !nodeValid -> "O nó $serverName não respondeu. Não é possível testar uma rota alternativa."
            else -> "Teste concluído: jogo ${direct.pingMs} ms; nó ${node.pingMs} ms. São destinos diferentes, por isso ainda não é possível confirmar redução do ping do jogo."
        }
        return RouteComparison(
            directMetrics = direct,
            optimizedMetrics = direct,
            latencyDiffMs = 0,
            latencyDiffPercent = 0f,
            jitterDiffMs = 0,
            isOptimizedBetter = false,
            recommendationMessage = message
        )
    }
}
