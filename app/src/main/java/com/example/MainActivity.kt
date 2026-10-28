package com.example

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.model.VpnStatus
import com.example.ui.components.RouteBoostBottomNav
import com.example.ui.components.RouteBoostTopBar
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.RouteBoostTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavigationScreen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RouteBoostTheme {
                val uiState by viewModel.uiState.collectAsState()
                val games by viewModel.games.collectAsState()
                val servers by viewModel.servers.collectAsState()
                val testHistory by viewModel.testHistory.collectAsState()
                val isPingingServers by viewModel.isPingingServers.collectAsState()
                val context = LocalContext.current

                // Launcher para pedido oficial de permissão de VPN do Android
                val vpnPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        viewModel.startOptimization(context)
                    } else {
                        Toast.makeText(
                            context,
                            "Permissão de VPN necessária para otimizar as rotas do jogo.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                val handleBoostToggle = {
                    if (uiState.vpnStatus == VpnStatus.ACTIVE || uiState.vpnStatus == VpnStatus.CONNECTING) {
                        viewModel.stopOptimization(context)
                    } else {
                        val prepareIntent = VpnService.prepare(context)
                        if (prepareIntent != null) {
                            vpnPermissionLauncher.launch(prepareIntent)
                        } else {
                            viewModel.startOptimization(context)
                        }
                    }
                }

                // Navegação com BackHandler para regressar ao Home
                if (uiState.currentScreen != NavigationScreen.HOME) {
                    BackHandler {
                        viewModel.setScreen(NavigationScreen.HOME)
                    }
                }

                Scaffold(
                    topBar = {
                        RouteBoostTopBar(
                            vpnStatus = uiState.vpnStatus,
                            onTestClick = { viewModel.setScreen(NavigationScreen.DIAGNOSTICS) }
                        )
                    },
                    bottomBar = {
                        RouteBoostBottomNav(
                            currentScreen = uiState.currentScreen,
                            onScreenSelected = { viewModel.setScreen(it) }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentScreen) {
                            NavigationScreen.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onBoostToggle = handleBoostToggle,
                                    onRunBenchmark = { viewModel.runRouteBenchmark() },
                                    onNavigate = { viewModel.setScreen(it) }
                                )
                            }
                            NavigationScreen.GAMES -> {
                                GamesScreen(
                                    games = games,
                                    selectedGame = uiState.selectedGame,
                                    searchQuery = uiState.searchQuery,
                                    selectedCategory = uiState.selectedCategory,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategoryChange = { viewModel.setSelectedCategory(it) },
                                    onGameSelected = { game ->
                                        viewModel.selectGame(game)
                                        viewModel.setScreen(NavigationScreen.HOME)
                                    },
                                    onAddCustomGame = { name, pkg, host, port, emoji ->
                                        viewModel.addCustomGame(name, pkg, host, port, emoji)
                                    }
                                )
                            }
                            NavigationScreen.SERVERS -> {
                                ServersScreen(
                                    servers = servers,
                                    selectedServer = uiState.selectedServer,
                                    isAutoRouteEnabled = uiState.isAutoRouteEnabled,
                                    selectedRegion = uiState.selectedRegion,
                                    isPinging = isPingingServers,
                                    onRegionChange = { viewModel.setSelectedRegion(it) },
                                    onAutoRouteToggle = { viewModel.toggleAutoRoute(it) },
                                    onServerSelected = { server ->
                                        viewModel.selectServer(server)
                                        viewModel.toggleAutoRoute(false)
                                        viewModel.setScreen(NavigationScreen.HOME)
                                    },
                                    onPingAllServers = { viewModel.pingAllServers() },
                                    onAddCustomServer = { name, city, country, host, port ->
                                        viewModel.addCustomServer(name, city, country, host, port)
                                    }
                                )
                            }
                            NavigationScreen.DIAGNOSTICS -> {
                                DiagnosticsScreen(
                                    selectedGame = uiState.selectedGame,
                                    metrics = uiState.currentMetrics,
                                    isBenchmarking = uiState.isBenchmarking,
                                    testHistory = testHistory,
                                    onRunBenchmark = { viewModel.runRouteBenchmark() },
                                    onClearHistory = { viewModel.clearTestHistory() }
                                )
                            }
                            NavigationScreen.SETTINGS -> {
                                SettingsScreen(
                                    isSplitTunnelEnabled = uiState.isSplitTunnelEnabled,
                                    mtuSize = uiState.mtuSize,
                                    gamingDns = uiState.gamingDns,
                                    onSplitTunnelToggle = { viewModel.toggleSplitTunnel(it) },
                                    onMtuChange = { viewModel.setMtu(it) },
                                    onDnsChange = { viewModel.setDns(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
