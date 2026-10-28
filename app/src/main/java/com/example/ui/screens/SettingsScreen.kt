package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.OrangeBoost
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.PingGreen
import com.example.ui.theme.PingYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    isSplitTunnelEnabled: Boolean,
    mtuSize: Int,
    gamingDns: String,
    onSplitTunnelToggle: (Boolean) -> Unit,
    onMtuChange: (Int) -> Unit,
    onDnsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showServerGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Configurações de Rede",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Parâmetros do tunelamento VpnService e rotas",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Split-Tunneling
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Split-Tunneling (Jogo Único)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Encaminha apenas pacotes do jogo selecionado",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isSplitTunnelEnabled,
                        onCheckedChange = onSplitTunnelToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = CyanPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkSurface
                        ),
                        modifier = Modifier.testTag("split_tunnel_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSplitTunnelEnabled)
                        "✓ Ativado: WhatsApp, navegador e vídeos continuam na sua internet normal sem consumir dados do túnel."
                    else
                        "⚠️ Desativado: Todo o tráfego do telemóvel passará pelo nó de roteamento.",
                    color = if (isSplitTunnelEnabled) PingGreen else PingYellow,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MTU (Maximum Transmission Unit)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Tamanho de Pacote (MTU)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Evita fragmentação de pacotes e perda por retransmissão",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MtuOption(
                        label = "1420 (Recomendado)",
                        sub = "WireGuard / 4G / 5G",
                        selected = mtuSize == 1420,
                        onClick = { onMtuChange(1420) },
                        modifier = Modifier.weight(1f)
                    )
                    MtuOption(
                        label = "1280 (Mínimo)",
                        sub = "IPv6 / Redes Instáveis",
                        selected = mtuSize == 1280,
                        onClick = { onMtuChange(1280) },
                        modifier = Modifier.weight(1f)
                    )
                    MtuOption(
                        label = "1500 (Padrão)",
                        sub = "Wi-Fi Fibra Direto",
                        selected = mtuSize == 1500,
                        onClick = { onMtuChange(1500) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Servidor DNS para Jogos
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = OrangeBoost,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Servidor DNS para Jogos",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                DnsOptionRow(
                    name = "Cloudflare 1.1.1.1",
                    ip = "1.1.1.1",
                    detail = "Menor latência de resolução global",
                    selected = gamingDns == "1.1.1.1",
                    onSelect = { onDnsChange("1.1.1.1") }
                )
                DnsOptionRow(
                    name = "Google Public DNS",
                    ip = "8.8.8.8",
                    detail = "Alta disponibilidade e anycast global",
                    selected = gamingDns == "8.8.8.8",
                    onSelect = { onDnsChange("8.8.8.8") }
                )
                DnsOptionRow(
                    name = "Quad9 DNS Seguro",
                    ip = "9.9.9.9",
                    detail = "Proteção e baixa latência",
                    selected = gamingDns == "9.9.9.9",
                    onSelect = { onDnsChange("9.9.9.9") }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Aviso Técnico: Mudar DNS não reduz o ping físico durante a partida; apenas acelera a conexão inicial aos servidores de matchmaking.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GUIA DE INFRAESTRUTURA GRATUITA E SERVIDORES
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(OrangeBoost.copy(alpha = 0.5f), OutlineBorder))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showServerGuide = !showServerGuide },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = OrangeBoost,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Como ter o seu Servidor Grátis (VPS)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Passo a passo 100% pelo telemóvel",
                                color = OrangeBoost,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showServerGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(visible = showServerGuide) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Por que precisamos de um servidor intermediário?",
                            color = CyanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Uma VPN local no telemóvel não consegue 'encurtar fios' sozinha. O segredo de programas como ExitLag é rotear os pacotes através de um servidor com trânsito de fibra premium (Tier-1) e rotas diretas para os servidores de jogos (AWS, Riot, Garena, EA), contornando congestionamentos da sua operadora.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Opções Gratuitas para Hospedar o seu Nó:",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Oracle Cloud Always Free (Recomendado):\n" +
                                    "   • 4 OCPUs ARM, 24 GB RAM e 10 TB tráfego grátis para sempre.\n" +
                                    "   • Data Centers em São Paulo, Santiago, Frankfurt, etc.\n" +
                                    "2. Fly.io ou Render (Free Tiers):\n" +
                                    "   • Permite rodar instâncias leves de WireGuard/UDP Relay.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Comando de 1 clique para instalar no Servidor:",
                            color = OrangeBoost,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val serverCommand = "curl -sSL https://raw.githubusercontent.com/routeboost/server/main/install.sh | bash"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, OutlineBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = serverCommand,
                                    color = PingGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar Comando",
                                    tint = CyanPrimary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("RouteBoost Command", serverCommand)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Comando copiado!", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Depois de rodar o comando, copie o IP do seu servidor e adicione-o na aba 'Servidores' do RouteBoost.",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segurança & Transparência
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(OutlineBorder, OutlineBorder))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PingGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Segurança & Conformidade",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "• Sem Root: Opera 100% através da API oficial Android VpnService.\n" +
                            "• Sem risco de Ban: Não modifica ficheiros de jogos nem injeta memória, atuando unicamente no encaminhamento de pacotes IP padrão.\n" +
                            "• Privacidade: Sem registo de passwords, contas ou dados pessoais.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MtuOption(
    label: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) CyanPrimary.copy(alpha = 0.15f) else DarkSurface)
            .border(
                1.dp,
                if (selected) CyanPrimary else OutlineBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                color = if (selected) CyanPrimary else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sub,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun DnsOptionRow(
    name: String,
    ip: String,
    detail: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = OrangeBoost,
                unselectedColor = TextMuted
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = "$name ($ip)",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}
