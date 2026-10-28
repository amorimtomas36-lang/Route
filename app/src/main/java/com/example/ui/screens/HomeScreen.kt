package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameModel
import com.example.model.ServerNode
import com.example.model.VpnStatus
import com.example.ui.components.BigBoostButton
import com.example.ui.components.RouteComparisonCard
import com.example.ui.components.TelemetryHud
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.OrangeBoost
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PingGreen
import com.example.ui.theme.PingRed
import com.example.ui.theme.PingYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NavigationScreen
import com.example.viewmodel.UiState

@Composable
fun HomeScreen(
    uiState: UiState,
    onBoostToggle: () -> Unit,
    onRunBenchmark: () -> Unit,
    onNavigate: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Alertas de erro se existirem
        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PingRed.copy(alpha = 0.15f))
                    .border(1.dp, PingRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PingRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Seletores rápidos de Jogo e Servidor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickSelectionCard(
                title = "JOGO ALVO",
                name = uiState.selectedGame?.name ?: "Selecionar Jogo",
                detail = if (uiState.selectedGame?.isInstalled == true) "Instalado no telemóvel" else "Servidor Oficial",
                iconEmoji = uiState.selectedGame?.iconEmoji ?: "🎮",
                onClick = { onNavigate(NavigationScreen.GAMES) },
                testTag = "home_select_game_card",
                modifier = Modifier.weight(1f)
            )

            QuickSelectionCard(
                title = "ROTA DE SAÍDA",
                name = if (uiState.isAutoRouteEnabled) "Melhor Rota Auto" else (uiState.selectedServer?.city ?: "Servidor"),
                detail = if (uiState.isAutoRouteEnabled) "Otimização Inteligente" else (uiState.selectedServer?.country ?: "Nó Fixo"),
                iconEmoji = uiState.selectedServer?.flagEmoji ?: "🌐",
                onClick = { onNavigate(NavigationScreen.SERVERS) },
                testTag = "home_select_server_card",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grande botão central de Boost
        BigBoostButton(
            status = uiState.vpnStatus,
            uptimeSeconds = uiState.uptimeSeconds,
            onClick = onBoostToggle
        )

        if (uiState.isBenchmarking) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.benchmarkStatusText,
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    color = CyanPrimary,
                    trackColor = DarkSurfaceElevated,
                    modifier = Modifier
                        .width(180.dp)
                        .height(3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cartões HUD de Telemetria em tempo real
        TelemetryHud(
            metrics = uiState.currentMetrics,
            isBenchmarking = uiState.isBenchmarking
        )

        // Estatísticas de tráfego se ativo
        if (uiState.vpnStatus == VpnStatus.ACTIVE) {
            Spacer(modifier = Modifier.height(16.dp))
            TrafficStatsCard(
                rxBytes = uiState.bytesReceived,
                txBytes = uiState.bytesSent,
                packets = uiState.packetsProcessed,
                splitTunnel = uiState.isSplitTunnelEnabled,
                gameName = uiState.selectedGame?.name ?: "Jogo"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Comparação de Rota Antes vs Depois
        RouteComparisonCard(
            comparison = uiState.routeComparison,
            onRunBenchmark = onRunBenchmark
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun QuickSelectionCard(
    title: String,
    name: String,
    detail: String,
    iconEmoji: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(text = iconEmoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                color = CyanPrimary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrafficStatsCard(
    rxBytes: Long,
    txBytes: Long,
    packets: Long,
    splitTunnel: Boolean,
    gameName: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRÁFEGO DO TÚNEL TUN",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (splitTunnel) "Split-Tunnel: $gameName" else "Túnel Global",
                    color = OrangeBoost,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Pacotes Processados", color = TextMuted, fontSize = 10.sp)
                    Text(text = "$packets pkts", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    val formattedKb = (txBytes + rxBytes) / 1024
                    Text(text = "Volume Transferido", color = TextMuted, fontSize = 10.sp)
                    Text(text = "$formattedKb KB", color = PingGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
