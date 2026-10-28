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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameModel
import com.example.model.RouteMetrics
import com.example.model.TestHistoryItem
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    selectedGame: GameModel?,
    metrics: RouteMetrics?,
    isBenchmarking: Boolean,
    testHistory: List<TestHistoryItem>,
    onRunBenchmark: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Diagnóstico & Auditoria",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Medições reais de sockets e análise RFC 3550",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (testHistory.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, OutlineBorder, RoundedCornerShape(8.dp))
                        .clickable { onClearHistory() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Limpar Histórico",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Painel de Teste de Rota Ativo
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ALVO ATUAL: ${selectedGame?.name ?: "Nenhum Jogo"}",
                            color = CyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Host: ${selectedGame?.targetHosts?.firstOrNull() ?: "1.1.1.1"}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onRunBenchmark,
                        enabled = !isBenchmarking,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("run_audit_button")
                    ) {
                        if (isBenchmarking) {
                            CircularProgressIndicator(
                                color = DarkBackground,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auditar",
                                color = DarkBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isBenchmarking) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        color = CyanPrimary,
                        trackColor = DarkSurfaceElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Técnico Detalhado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DiagnosticStat(
                        label = "Ping Mínimo",
                        value = if (metrics != null && metrics.minPingMs > 0) "${metrics.minPingMs}ms" else "--",
                        color = PingGreen
                    )
                    DiagnosticStat(
                        label = "Ping Médio",
                        value = if (metrics != null && metrics.pingMs > 0) "${metrics.pingMs}ms" else "--",
                        color = TextPrimary
                    )
                    DiagnosticStat(
                        label = "Ping Máximo (Pico)",
                        value = if (metrics != null && metrics.maxPingMs > 0) "${metrics.maxPingMs}ms" else "--",
                        color = if ((metrics?.maxPingMs ?: 0) > 100) PingRed else PingYellow
                    )
                    DiagnosticStat(
                        label = "Jitter (RFC)",
                        value = if (metrics != null) "±${metrics.jitterMs}ms" else "--",
                        color = if ((metrics?.jitterMs ?: 0) <= 3) PingGreen else PingYellow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Histórico de Testes
        Text(
            text = "Histórico de Comparações de Rota",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (testHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sem testes gravados ainda",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Execute uma auditoria acima para medir a rota antes e depois.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(testHistory, key = { it.id }) { item ->
                    HistoryCardItem(item = item, dateFormatted = dateFormat.format(Date(item.timestamp)))
                }
            }
        }
    }
}

@Composable
private fun DiagnosticStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HistoryCardItem(
    item: TestHistoryItem,
    dateFormatted: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.gameName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $dateFormatted",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "Via: ${item.serverName}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Comparação
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Direto: ${item.directPingMs}ms",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Boost: ${item.boostedPingMs}ms (Jitter: ±${item.boostedJitterMs}ms)",
                        color = if (item.improved) PingGreen else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (item.improved) PingGreen.copy(alpha = 0.15f)
                            else DarkSurface
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (item.improved) "Otimizado" else "Direto Ótimo",
                        color = if (item.improved) PingGreen else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
