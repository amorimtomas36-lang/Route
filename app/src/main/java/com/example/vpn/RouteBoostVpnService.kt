package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.VpnStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Serviço VPN nativo do Android para criação de interface de tunelamento TUN,
 * suporte a Split-Tunneling por aplicação de jogo e proteção de rotas.
 */
class RouteBoostVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var tunnelJob: Job? = null
    private var metricsJob: Job? = null

    private var totalBytesIn = 0L
    private var totalBytesOut = 0L
    private var totalPackets = 0L

    companion object {
        const val ACTION_START = "com.example.routeboost.START_VPN"
        const val ACTION_STOP = "com.example.routeboost.STOP_VPN"

        const val EXTRA_GAME_PACKAGE = "extra_game_package"
        const val EXTRA_GAME_NAME = "extra_game_name"
        const val EXTRA_SERVER_HOST = "extra_server_host"
        const val EXTRA_SERVER_PORT = "extra_server_port"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SPLIT_TUNNEL = "extra_split_tunnel"
        const val EXTRA_MTU = "extra_mtu"
        const val EXTRA_DNS = "extra_dns"

        private const val CHANNEL_ID = "routeboost_vpn_status_channel"
        private const val NOTIFICATION_ID = 4040
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                val gamePackage = intent.getStringExtra(EXTRA_GAME_PACKAGE) ?: ""
                val gameName = intent.getStringExtra(EXTRA_GAME_NAME) ?: "Jogo Selecionado"
                val serverName = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Servidor Ótimo"
                val serverHost = intent.getStringExtra(EXTRA_SERVER_HOST) ?: "1.1.1.1"
                val serverPort = intent.getIntExtra(EXTRA_SERVER_PORT, 51820)
                val splitTunnel = intent.getBooleanExtra(EXTRA_SPLIT_TUNNEL, true)
                val mtu = intent.getIntExtra(EXTRA_MTU, 1420)
                val dns = intent.getStringExtra(EXTRA_DNS) ?: "1.1.1.1"

                startVpnTunnel(gamePackage, gameName, serverName, serverHost, serverPort, splitTunnel, mtu, dns)
            }
            ACTION_STOP -> {
                stopVpnTunnel()
            }
        }

        return START_NOT_STICKY
    }

    private fun startVpnTunnel(
        gamePackage: String,
        gameName: String,
        serverName: String,
        serverHost: String,
        serverPort: Int,
        splitTunnel: Boolean,
        mtu: Int,
        dns: String
    ) {
        VpnStateManager.updateStatus(VpnStatus.CONNECTING)

        activeRelayHost = serverHost
        activeRelayPort = serverPort
        val notification = buildForegroundNotification(gameName, serverName)
        startForeground(NOTIFICATION_ID, notification)

        try {
            val builder = Builder()
                .setSession("RouteBoost: $gameName")
                .setMtu(mtu.coerceIn(1280, 1500))
                .addAddress("10.150.0.2", 24)
                .addDnsServer(dns)
                .addRoute("0.0.0.0", 0)

            // Split-Tunneling: Aplica a rota otimizada EXCLUSIVAMENTE ao jogo selecionado
            // mantendo o tráfego de outros apps (navegador, WhatsApp, downloads) intactos
            if (splitTunnel && gamePackage.isNotBlank()) {
                try {
                    builder.addAllowedApplication(gamePackage)
                } catch (e: PackageManager.NameNotFoundException) {
                    // Jogo não está instalado localmente, permite fallback
                }
            }

            // Exclui o próprio app do túnel para evitar loops de tráfego
            try {
                builder.addDisallowedApplication(packageName)
            } catch (_: Exception) {
            }

            vpnInterface?.close()
            vpnInterface = builder.establish()

            if (vpnInterface == null) {
                VpnStateManager.updateStatus(
                    VpnStatus.ERROR,
                    "Não foi possível estabelecer a interface TUN. Verifique permissões."
                )
                stopSelf()
                return
            }

            VpnStateManager.updateStatus(VpnStatus.ACTIVE)
            VpnStateManager.resetUptime()

            startPacketLoop(vpnInterface!!)
            startTelemetryMonitor()

        } catch (e: Exception) {
            VpnStateManager.updateStatus(
                VpnStatus.ERROR,
                "Erro ao inicializar o túnel VPN: ${e.localizedMessage}"
            )
            stopSelf()
        }
    }

    private var udpForwarder: UdpTunForwarder? = null
    private var activeRelayHost: String? = null
    private var activeRelayPort: Int = 7000

    private fun startPacketLoop(pfd: ParcelFileDescriptor) {
        tunnelJob?.cancel()
        tunnelJob = serviceScope.launch {
            val buffer = ByteArray(32767)
            val inputStream = FileInputStream(pfd.fileDescriptor)
            val outputStream = FileOutputStream(pfd.fileDescriptor)
            udpForwarder = UdpTunForwarder(
                vpnService = this@RouteBoostVpnService,
                tunOutput = outputStream,
                scope = serviceScope,
                relayHost = activeRelayHost,
                relayPort = activeRelayPort
            )

            try {
                while (isActive) {
                    val length = inputStream.read(buffer)
                    if (length > 0) {
                        val forwarded = udpForwarder?.handlePacket(buffer, length) ?: 0
                        totalBytesOut += length
                        totalPackets++
                        if (forwarded > 0) {
                            totalBytesIn += forwarded
                        }
                        VpnStateManager.updateTraffic(totalBytesIn, totalBytesOut, totalPackets)
                    } else if (length < 0) {
                        break
                    }
                }
            } catch (e: IOException) {
                // Interface fechada normalmente
            } finally {
                udpForwarder?.close()
                udpForwarder = null
                try {
                    inputStream.close()
                    outputStream.close()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun startTelemetryMonitor() {
        metricsJob?.cancel()
        metricsJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                VpnStateManager.incrementUptime()
            }
        }
    }

    private fun stopVpnTunnel() {
        tunnelJob?.cancel()
        metricsJob?.cancel()
        udpForwarder?.close()
        udpForwarder = null

        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }
        vpnInterface = null

        VpnStateManager.updateStatus(VpnStatus.DISCONNECTED)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Estado do RouteBoost",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação ativa durante a otimização de rotas para jogos"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(gameName: String, serverName: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, RouteBoostVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RouteBoost Ativo • $gameName")
            .setContentText("Encaminhando via $serverName")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Desconectar", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        stopVpnTunnel()
    }
}
