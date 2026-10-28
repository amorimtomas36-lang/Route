package com.example.network

import com.example.model.RouteMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Utilitário para medição real de latência de rede (RTT), jitter e perda de pacotes
 * utilizando ligações diretas por socket TCP de alta precisão.
 */
object NetworkLatencyProbe {

    /**
     * Mede uma série de sondagens (samples) para um determinado host e porta,
     * calculando com exatidão a latência mínima, máxima, média, jitter e perda de pacotes.
     */
    suspend fun measureHost(
        host: String,
        port: Int = 443,
        samples: Int = 5,
        timeoutMs: Int = 2000
    ): RouteMetrics = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Int>()
        var failedCount = 0

        for (i in 0 until samples) {
            val rtt = measureSingleProbe(host, port, timeoutMs)
            if (rtt != null) {
                latencies.add(rtt)
            } else {
                failedCount++
            }
            // Pequeno intervalo entre pacotes para medição realista de jitter
            if (i < samples - 1) {
                kotlinx.coroutines.delay(50)
            }
        }

        if (latencies.isEmpty()) {
            return@withContext RouteMetrics(
                pingMs = -1,
                minPingMs = -1,
                maxPingMs = -1,
                jitterMs = -1,
                packetLossPercent = 100f,
                samplesCount = samples,
                stabilityScore = 0
            )
        }

        val avgPing = latencies.average().roundToInt()
        val minPing = latencies.minOrNull() ?: avgPing
        val maxPing = latencies.maxOrNull() ?: avgPing

        // Cálculo do Jitter baseado na diferença média absoluta entre amostras sucessivas (RFC 3550 / IETF)
        val jitter = if (latencies.size > 1) {
            var diffSum = 0
            for (j in 0 until latencies.size - 1) {
                diffSum += abs(latencies[j + 1] - latencies[j])
            }
            (diffSum.toFloat() / (latencies.size - 1)).roundToInt()
        } else {
            0
        }

        val lossPercent = (failedCount.toFloat() / samples.toFloat()) * 100f

        // Índice de estabilidade (100 = perfeito, penaliza jitter e perda de pacotes)
        val stabilityPenalty = (jitter * 2.5f) + (lossPercent * 2.0f)
        val stabilityScore = (100f - stabilityPenalty).coerceIn(0f, 100f).roundToInt()

        RouteMetrics(
            pingMs = avgPing,
            minPingMs = minPing,
            maxPingMs = maxPing,
            jitterMs = jitter,
            packetLossPercent = lossPercent,
            samplesCount = samples,
            stabilityScore = stabilityScore
        )
    }

    /**
     * Efetua um teste individual de latência por socket.
     * Retorna a duração em milissegundos ou null em caso de timeout/falha.
     */
    suspend fun measureSingleProbe(
        host: String,
        port: Int = 443,
        timeoutMs: Int = 2000
    ): Int? = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        val startNano = System.nanoTime()
        try {
            socket = Socket()
            socket.tcpNoDelay = true
            socket.soTimeout = timeoutMs
            val address = InetSocketAddress(host, port)
            socket.connect(address, timeoutMs)
            val elapsedMs = ((System.nanoTime() - startNano) / 1_000_000.0).roundToInt()
            elapsedMs.coerceAtLeast(1)
        } catch (e: SocketTimeoutException) {
            null
        } catch (e: IOException) {
            // Nota: Se a porta estiver fechada pelo servidor remoto mas o host responder com TCP RST,
            // o handshake ainda percorreu a rede de ida e volta (RTT real da rota).
            val elapsedMs = ((System.nanoTime() - startNano) / 1_000_000.0).roundToInt()
            if (elapsedMs in 1 until timeoutMs) {
                elapsedMs
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
        }
    }
}
