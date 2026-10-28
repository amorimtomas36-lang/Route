package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameModel
import com.example.model.RouteComparison
import com.example.model.RouteMetrics
import com.example.model.ServerNode
import com.example.model.VpnStatus
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
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

/**
 * TopBar com marca RouteBoost e indicador de estado ao vivo.
 */
@Composable
fun RouteBoostTopBar(
    vpnStatus: VpnStatus,
    onTestClick: () -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CyanPrimary, OrangeBoost)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "RouteBoost",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Gaming Network Optimizer",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Indicador de estado
            val (badgeBg, badgeTextColor, badgeLabel) = when (vpnStatus) {
                VpnStatus.ACTIVE -> Triple(PingGreen.copy(alpha = 0.2f), PingGreen, "OTIMIZADO")
                VpnStatus.CONNECTING -> Triple(OrangeBoost.copy(alpha = 0.2f), OrangeBoost, "A LIGAR...")
                VpnStatus.BENCHMARKING -> Triple(CyanPrimary.copy(alpha = 0.2f), CyanPrimary, "A TESTAR")
                VpnStatus.ERROR -> Triple(PingRed.copy(alpha = 0.2f), PingRed, "ERRO")
                VpnStatus.DISCONNECTED -> Triple(TextMuted.copy(alpha = 0.2f), TextMuted, "DESCONECTADO")
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBg)
                    .border(1.dp, badgeTextColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { onTestClick() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(badgeTextColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeLabel,
                        color = badgeTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Grande botão de controlo de otimização de rotas com animação de pulsar.
 */
@Composable
fun BigBoostButton(
    status: VpnStatus,
    uptimeSeconds: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = status == VpnStatus.ACTIVE
    val isConnecting = status == VpnStatus.CONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive || isConnecting) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(190.dp)
        ) {
            // Anel exterior de brilho
            if (isActive || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isActive) CyanPrimary.copy(alpha = 0.25f) else OrangeBoost.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Anel delimitador
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            if (isActive) listOf(CyanPrimary, OrangeBoost, CyanPrimary)
                            else listOf(OutlineBorder, OutlineBorder)
                        ),
                        shape = CircleShape
                    )
                    .clickable { onClick() }
                    .testTag("boost_action_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = OrangeBoost,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A CONECTAR...",
                            color = OrangeBoost,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Botão Boost",
                            tint = if (isActive) CyanPrimary else TextMuted,
                            modifier = Modifier.size(46.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isActive) "PARAR BOOST" else "OTIMIZAR ROTA",
                            color = if (isActive) CyanPrimary else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        if (isActive) {
                            val mins = uptimeSeconds / 60
                            val secs = uptimeSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Toque para ligar",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grelha de cartões HUD para exibição de métricas reais de rede.
 */
@Composable
fun TelemetryHud(
    metrics: RouteMetrics?,
    isBenchmarking: Boolean,
    modifier: Modifier = Modifier
) {
    val ping = metrics?.pingMs ?: -1
    val jitter = metrics?.jitterMs ?: 0
    val loss = metrics?.packetLossPercent ?: 0f
    val score = metrics?.stabilityScore ?: 0

    val pingColor = when {
        ping <= 0 -> TextMuted
        ping < 45 -> PingGreen
        ping < 90 -> PingYellow
        else -> PingRed
    }

    val stabilityColor = when {
        score >= 85 -> PingGreen
        score >= 65 -> PingYellow
        else -> PingRed
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricItem(
            title = "LATÊNCIA (RTT)",
            value = if (ping > 0) "${ping}ms" else "--",
            subtitle = if (metrics != null && metrics.minPingMs > 0) "Mín ${metrics.minPingMs} / Máx ${metrics.maxPingMs}ms" else "Sondagem Socket",
            valueColor = pingColor,
            icon = Icons.Default.Speed,
            isLoading = isBenchmarking,
            modifier = Modifier.weight(1f)
        )

        MetricItem(
            title = "JITTER",
            value = if (metrics != null && metrics.pingMs > 0) "±${jitter}ms" else "--",
            subtitle = if (jitter <= 3) "Oscilação Mínima" else "Variação de Rota",
            valueColor = if (jitter <= 4) PingGreen else PingYellow,
            icon = Icons.Default.Bolt,
            isLoading = isBenchmarking,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricItem(
            title = "PERDA PACOTES",
            value = if (metrics != null) "${loss.toInt()}%" else "0%",
            subtitle = if (loss == 0f) "0 drops detetados" else "Reenvio necessário",
            valueColor = if (loss == 0f) PingGreen else PingRed,
            icon = Icons.Default.Shield,
            isLoading = isBenchmarking,
            modifier = Modifier.weight(1f)
        )

        MetricItem(
            title = "ESTABILIDADE",
            value = if (metrics != null && metrics.pingMs > 0) "$score%" else "--",
            subtitle = if (score > 80) "Excelente para jogos" else "Sujeito a picos",
            valueColor = stabilityColor,
            icon = Icons.Default.CheckCircle,
            isLoading = isBenchmarking,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricItem(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    icon: ImageVector,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = valueColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isLoading) {
                LinearProgressIndicator(
                    color = CyanPrimary,
                    trackColor = DarkSurfaceElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(vertical = 4.dp)
                )
            } else {
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Cartão de comparação honesta: Rota Direta vs Rota com RouteBoost.
 */
@Composable
fun RouteComparisonCard(
    comparison: RouteComparison?,
    onRunBenchmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (comparison?.isOptimizedBetter == true) listOf(CyanPrimary.copy(alpha = 0.5f), OutlineBorder)
                else listOf(OutlineBorder, OutlineBorder)
            )
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUDITORIA REAL DE ROTA",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .clickable { onRunBenchmark() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Testar Novamente",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (comparison == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Toque em 'Testar Novamente' para medir a rota direta e avaliar nós alternativos em tempo real.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // Linha de Comparação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Rota Direta
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Rota Direta (Normal)", color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${comparison.directMetrics.pingMs} ms",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Jitter: ±${comparison.directMetrics.jitterMs}ms",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    // Rota RouteBoost
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Com RouteBoost", color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${comparison.optimizedMetrics.pingMs} ms",
                            color = if (comparison.isOptimizedBetter) CyanPrimary else TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Jitter: ±${comparison.optimizedMetrics.jitterMs}ms",
                            color = PingGreen,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mensagem técnica honesta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (comparison.isOptimizedBetter) CyanPrimary.copy(alpha = 0.1f)
                            else DarkSurfaceElevated
                        )
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (comparison.isOptimizedBetter) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (comparison.isOptimizedBetter) CyanPrimary else PingYellow,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = comparison.recommendationMessage,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}
