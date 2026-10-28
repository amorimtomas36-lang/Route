package com.example.model

/**
 * Representa um jogo online suportado ou personalizado pelo utilizador.
 */
data class GameModel(
    val id: String,
    val name: String,
    val packageName: String,
    val publisher: String,
    val category: GameCategory,
    val targetHosts: List<String>,
    val defaultPort: Int = 443,
    val iconEmoji: String = "🎮",
    val isInstalled: Boolean = false,
    val lastPingMs: Int? = null
)

enum class GameCategory(val label: String) {
    ALL("Todos"),
    INSTALLED("Instalados"),
    BATTLE_ROYALE("Battle Royale"),
    MOBA("MOBA"),
    FPS("FPS"),
    OUTROS("Outros")
}

/**
 * Representa um nó de servidor intermediário para otimização de rota.
 */
data class ServerNode(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val region: ServerRegion,
    val host: String,
    val port: Int = 51820,
    val protocol: ServerProtocol = ServerProtocol.WIREGUARD,
    val flagEmoji: String = "🌐",
    var currentPingMs: Int? = null,
    var jitterMs: Int? = null,
    val loadPercent: Int = 25,
    val isOnline: Boolean = true,
    val isCustom: Boolean = false,
    val isBenchmarkEndpoint: Boolean = false
)

enum class ServerRegion(val label: String) {
    SOUTH_AMERICA("América do Sul"),
    NORTH_AMERICA("América do Norte"),
    EUROPE("Europa"),
    ASIA("Ásia"),
    CUSTOM("Personalizados")
}

enum class ServerProtocol(val label: String) {
    WIREGUARD("WireGuard"),
    UDP_RELAY("UDP Relay Otimizado"),
    SMART_ROUTE("Smart Direct Peering")
}

/**
 * Métricas reais recolhidas de sondagem de rede.
 */
data class RouteMetrics(
    val pingMs: Int,
    val minPingMs: Int,
    val maxPingMs: Int,
    val jitterMs: Int,
    val packetLossPercent: Float,
    val samplesCount: Int,
    val stabilityScore: Int, // 0 a 100
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Comparação real entre a Rota Direta e a Rota com RouteBoost.
 */
data class RouteComparison(
    val directMetrics: RouteMetrics,
    val optimizedMetrics: RouteMetrics,
    val latencyDiffMs: Int,
    val latencyDiffPercent: Float,
    val jitterDiffMs: Int,
    val isOptimizedBetter: Boolean,
    val recommendationMessage: String
)

/**
 * Estado atual da VPN e do otimizador de rota.
 */
enum class VpnStatus {
    DISCONNECTED,
    CONNECTING,
    ACTIVE,
    BENCHMARKING,
    ERROR
}

/**
 * Registo de histórico de testes realizados.
 */
data class TestHistoryItem(
    val id: String,
    val gameName: String,
    val serverName: String,
    val directPingMs: Int,
    val boostedPingMs: Int,
    val directJitterMs: Int,
    val boostedJitterMs: Int,
    val packetLossPercent: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val improved: Boolean
)
