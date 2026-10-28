package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.model.GameCategory
import com.example.model.GameModel
import com.example.model.RouteComparison
import com.example.model.RouteMetrics
import com.example.model.ServerNode
import com.example.model.ServerRegion
import com.example.model.TestHistoryItem
import com.example.model.VpnStatus
import com.example.network.GameRegistry
import com.example.network.NetworkLatencyProbe
import com.example.network.RouteOptimizerEngine
import com.example.network.ServerRegistry
import com.example.vpn.RouteBoostVpnService
import com.example.vpn.VpnStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationScreen {
    HOME,
    GAMES,
    SERVERS,
    DIAGNOSTICS,
    SETTINGS
}

data class UiState(
    val vpnStatus: VpnStatus = VpnStatus.DISCONNECTED,
    val selectedGame: GameModel? = null,
    val selectedServer: ServerNode? = null,
    val isAutoRouteEnabled: Boolean = true,
    val isSplitTunnelEnabled: Boolean = true,
    val mtuSize: Int = 1420,
    val gamingDns: String = "1.1.1.1",
    val currentMetrics: RouteMetrics? = null,
    val routeComparison: RouteComparison? = null,
    val isBenchmarking: Boolean = false,
    val benchmarkStatusText: String = "",
    val currentScreen: NavigationScreen = NavigationScreen.HOME,
    val searchQuery: String = "",
    val selectedCategory: GameCategory = GameCategory.ALL,
    val selectedRegion: ServerRegion = ServerRegion.SOUTH_AMERICA,
    val uptimeSeconds: Long = 0L,
    val bytesReceived: Long = 0L,
    val bytesSent: Long = 0L,
    val packetsProcessed: Long = 0L,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)

    private val _uiState = MutableStateFlow(
        UiState(
            isAutoRouteEnabled = prefs.isAutoRouteEnabled,
            isSplitTunnelEnabled = prefs.isSplitTunnelEnabled,
            mtuSize = prefs.mtuSize,
            gamingDns = prefs.gamingDns
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _games = MutableStateFlow<List<GameModel>>(emptyList())
    val games: StateFlow<List<GameModel>> = _games.asStateFlow()

    private val _servers = MutableStateFlow<List<ServerNode>>(emptyList())
    val servers: StateFlow<List<ServerNode>> = _servers.asStateFlow()

    private val _testHistory = MutableStateFlow<List<TestHistoryItem>>(emptyList())
    val testHistory: StateFlow<List<TestHistoryItem>> = _testHistory.asStateFlow()

    private val _isPingingServers = MutableStateFlow(false)
    val isPingingServers: StateFlow<Boolean> = _isPingingServers.asStateFlow()

    init {
        loadData()
        observeVpnState()
    }

    private fun loadData() {
        val app = getApplication<Application>()
        val customGames = prefs.getCustomGames()
        val loadedGames = GameRegistry.getGamesWithInstalledStatus(app, customGames)
        _games.value = loadedGames

        val customServers = prefs.getCustomServers()
        val loadedServers = ServerRegistry.getAllServers(customServers)
        _servers.value = loadedServers

        _testHistory.value = prefs.getTestHistory()

        // Seleciona o jogo e servidor salvos
        val targetGame = loadedGames.firstOrNull { it.id == prefs.selectedGameId } ?: loadedGames.firstOrNull()
        val targetServer = loadedServers.firstOrNull { it.id == prefs.selectedServerId } ?: loadedServers.firstOrNull()

        _uiState.value = _uiState.value.copy(
            selectedGame = targetGame,
            selectedServer = targetServer
        )

        // Sondagem inicial em background do jogo selecionado
        targetGame?.let {
            viewModelScope.launch {
                val ping = NetworkLatencyProbe.measureSingleProbe(it.targetHosts.firstOrNull() ?: "1.1.1.1", it.defaultPort)
                if (ping != null) {
                    _uiState.value = _uiState.value.copy(
                        currentMetrics = RouteMetrics(
                            pingMs = ping,
                            minPingMs = ping,
                            maxPingMs = ping,
                            jitterMs = 2,
                            packetLossPercent = 0f,
                            samplesCount = 1,
                            stabilityScore = 95
                        )
                    )
                }
            }
        }
    }

    private fun observeVpnState() {
        viewModelScope.launch {
            VpnStateManager.status.collect { status ->
                _uiState.value = _uiState.value.copy(
                    vpnStatus = status,
                    errorMessage = VpnStateManager.errorMessage.value
                )
            }
        }
        viewModelScope.launch {
            VpnStateManager.uptimeSeconds.collect { uptime ->
                _uiState.value = _uiState.value.copy(uptimeSeconds = uptime)
            }
        }
        viewModelScope.launch {
            combine(
                VpnStateManager.bytesReceived,
                VpnStateManager.bytesSent,
                VpnStateManager.packetsProcessed
            ) { rx, tx, packets ->
                Triple(rx, tx, packets)
            }.collect { (rx, tx, packets) ->
                _uiState.value = _uiState.value.copy(
                    bytesReceived = rx,
                    bytesSent = tx,
                    packetsProcessed = packets
                )
            }
        }
    }

    fun setScreen(screen: NavigationScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedCategory(category: GameCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSelectedRegion(region: ServerRegion) {
        _uiState.value = _uiState.value.copy(selectedRegion = region)
    }

    fun selectGame(game: GameModel) {
        prefs.selectedGameId = game.id
        _uiState.value = _uiState.value.copy(selectedGame = game)
        // Dispara sondagem rápida
        viewModelScope.launch {
            val ping = NetworkLatencyProbe.measureSingleProbe(game.targetHosts.firstOrNull() ?: "1.1.1.1", game.defaultPort)
            if (ping != null) {
                _uiState.value = _uiState.value.copy(
                    currentMetrics = RouteMetrics(
                        pingMs = ping,
                        minPingMs = ping,
                        maxPingMs = ping,
                        jitterMs = 3,
                        packetLossPercent = 0f,
                        samplesCount = 1,
                        stabilityScore = 95
                    )
                )
            }
        }
    }

    fun selectServer(server: ServerNode) {
        prefs.selectedServerId = server.id
        _uiState.value = _uiState.value.copy(selectedServer = server)
    }

    fun toggleAutoRoute(enabled: Boolean) {
        prefs.isAutoRouteEnabled = enabled
        _uiState.value = _uiState.value.copy(isAutoRouteEnabled = enabled)
    }

    fun toggleSplitTunnel(enabled: Boolean) {
        prefs.isSplitTunnelEnabled = enabled
        _uiState.value = _uiState.value.copy(isSplitTunnelEnabled = enabled)
    }

    fun setMtu(mtu: Int) {
        prefs.mtuSize = mtu
        _uiState.value = _uiState.value.copy(mtuSize = mtu)
    }

    fun setDns(dns: String) {
        prefs.gamingDns = dns
        _uiState.value = _uiState.value.copy(gamingDns = dns)
    }

    /**
     * Efetua um teste rigoroso de rota antes de aplicar a otimização.
     */
    fun runRouteBenchmark() {
        val game = _uiState.value.selectedGame ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBenchmarking = true,
                benchmarkStatusText = "A medir latência direta para ${game.name}..."
            )

            val serverToTest = if (_uiState.value.isAutoRouteEnabled) null else _uiState.value.selectedServer
            val (comparison, bestServer) = RouteOptimizerEngine.benchmarkGameRoute(
                game = game,
                selectedServer = serverToTest,
                candidateServers = _servers.value
            )

            // Atualiza servidor se estivesse em modo automático
            if (_uiState.value.isAutoRouteEnabled) {
                selectServer(bestServer)
            }

            _uiState.value = _uiState.value.copy(
                routeComparison = comparison,
                currentMetrics = if (comparison.isOptimizedBetter) comparison.optimizedMetrics else comparison.directMetrics,
                isBenchmarking = false,
                benchmarkStatusText = ""
            )

            // Grava no histórico
            val historyItem = TestHistoryItem(
                id = UUID.randomUUID().toString(),
                gameName = game.name,
                serverName = bestServer.name,
                directPingMs = comparison.directMetrics.pingMs,
                boostedPingMs = comparison.optimizedMetrics.pingMs,
                directJitterMs = comparison.directMetrics.jitterMs,
                boostedJitterMs = comparison.optimizedMetrics.jitterMs,
                packetLossPercent = comparison.optimizedMetrics.packetLossPercent,
                improved = comparison.isOptimizedBetter
            )
            prefs.addTestHistoryItem(historyItem)
            _testHistory.value = prefs.getTestHistory()
        }
    }

    /**
     * Mede a latência real de todos os servidores em paralelo por sockets.
     */
    fun pingAllServers() {
        if (_isPingingServers.value) return
        viewModelScope.launch {
            _isPingingServers.value = true
            val updated = _servers.value.map { server ->
                async {
                    val rtt = NetworkLatencyProbe.measureSingleProbe(server.host, server.port, timeoutMs = 1500)
                    server.copy(currentPingMs = rtt ?: -1)
                }
            }.awaitAll()
            _servers.value = updated
            _isPingingServers.value = false
        }
    }

    /**
     * Inicia a VPN do RouteBoost após consentimento do utilizador.
     */
    fun startOptimization(context: Context) {
        val game = _uiState.value.selectedGame ?: return
        val server = _uiState.value.selectedServer ?: return

        VpnStateManager.setActiveConfig(game, server)

        val intent = Intent(context, RouteBoostVpnService::class.java).apply {
            action = RouteBoostVpnService.ACTION_START
            putExtra(RouteBoostVpnService.EXTRA_GAME_PACKAGE, game.packageName)
            putExtra(RouteBoostVpnService.EXTRA_GAME_NAME, game.name)
            putExtra(RouteBoostVpnService.EXTRA_SERVER_HOST, server.host)
            putExtra(RouteBoostVpnService.EXTRA_SERVER_PORT, server.port)
            putExtra(RouteBoostVpnService.EXTRA_SERVER_NAME, server.name)
            putExtra(RouteBoostVpnService.EXTRA_SPLIT_TUNNEL, _uiState.value.isSplitTunnelEnabled)
            putExtra(RouteBoostVpnService.EXTRA_MTU, _uiState.value.mtuSize)
            putExtra(RouteBoostVpnService.EXTRA_DNS, _uiState.value.gamingDns)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Pára a otimização de rotas e encerra o túnel VPN.
     */
    fun stopOptimization(context: Context) {
        val intent = Intent(context, RouteBoostVpnService::class.java).apply {
            action = RouteBoostVpnService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun addCustomGame(name: String, packageName: String, targetHost: String, port: Int, emoji: String) {
        val newGame = GameModel(
            id = "custom_" + System.currentTimeMillis(),
            name = name,
            packageName = packageName,
            publisher = "Personalizado",
            category = GameCategory.OUTROS,
            targetHosts = listOf(targetHost),
            defaultPort = port,
            iconEmoji = emoji.ifBlank { "🎮" }
        )
        prefs.saveCustomGame(newGame)
        loadData()
    }

    fun addCustomServer(name: String, city: String, country: String, host: String, port: Int) {
        val newServer = ServerNode(
            id = "custom_server_" + System.currentTimeMillis(),
            name = name,
            city = city,
            country = country,
            region = ServerRegion.CUSTOM,
            host = host,
            port = port,
            flagEmoji = "⚡",
            isCustom = true
        )
        prefs.saveCustomServer(newServer)
        loadData()
    }

    fun clearTestHistory() {
        prefs.clearHistory()
        _testHistory.value = emptyList()
    }
}
