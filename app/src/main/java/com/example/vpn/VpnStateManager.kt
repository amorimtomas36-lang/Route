package com.example.vpn

import com.example.model.GameModel
import com.example.model.RouteComparison
import com.example.model.RouteMetrics
import com.example.model.ServerNode
import com.example.model.VpnStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de estado partilhado entre o VpnService em segundo plano e a interface Compose.
 */
object VpnStateManager {

    private val _status = MutableStateFlow(VpnStatus.DISCONNECTED)
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    private val _activeGame = MutableStateFlow<GameModel?>(null)
    val activeGame: StateFlow<GameModel?> = _activeGame.asStateFlow()

    private val _activeServer = MutableStateFlow<ServerNode?>(null)
    val activeServer: StateFlow<ServerNode?> = _activeServer.asStateFlow()

    private val _currentMetrics = MutableStateFlow<RouteMetrics?>(null)
    val currentMetrics: StateFlow<RouteMetrics?> = _currentMetrics.asStateFlow()

    private val _routeComparison = MutableStateFlow<RouteComparison?>(null)
    val routeComparison: StateFlow<RouteComparison?> = _routeComparison.asStateFlow()

    private val _bytesReceived = MutableStateFlow(0L)
    val bytesReceived: StateFlow<Long> = _bytesReceived.asStateFlow()

    private val _bytesSent = MutableStateFlow(0L)
    val bytesSent: StateFlow<Long> = _bytesSent.asStateFlow()

    private val _packetsProcessed = MutableStateFlow(0L)
    val packetsProcessed: StateFlow<Long> = _packetsProcessed.asStateFlow()

    private val _uptimeSeconds = MutableStateFlow(0L)
    val uptimeSeconds: StateFlow<Long> = _uptimeSeconds.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateStatus(newStatus: VpnStatus, error: String? = null) {
        _status.value = newStatus
        _errorMessage.value = error
    }

    fun setActiveConfig(game: GameModel?, server: ServerNode?) {
        _activeGame.value = game
        _activeServer.value = server
    }

    fun updateMetrics(metrics: RouteMetrics) {
        _currentMetrics.value = metrics
    }

    fun updateComparison(comparison: RouteComparison) {
        _routeComparison.value = comparison
    }

    fun updateTraffic(received: Long, sent: Long, packets: Long) {
        _bytesReceived.value = received
        _bytesSent.value = sent
        _packetsProcessed.value = packets
    }

    fun incrementUptime() {
        _uptimeSeconds.value = _uptimeSeconds.value + 1
    }

    fun resetUptime() {
        _uptimeSeconds.value = 0L
        _bytesReceived.value = 0L
        _bytesSent.value = 0L
        _packetsProcessed.value = 0L
    }
}
