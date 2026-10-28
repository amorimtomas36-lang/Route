package com.example.vpn

import android.net.VpnService
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * Encaminhador de pacotes UDP e ICMP em espaço de utilizador (Userspace TUN Forwarder).
 * Suporta dois modos de operação com failover automático:
 * 1. Modo Nó Intermediário (Relay Proxy): Encaminha datagramas através do servidor RouteBoost remoto.
 * 2. Modo Direto Protegido: Encaminha diretamente ao cluster do jogo com prioridade de socket e proteção VPN.
 */
class UdpTunForwarder(
    private val vpnService: VpnService,
    private val tunOutput: FileOutputStream,
    private val scope: CoroutineScope,
    private val relayHost: String? = null,
    private val relayPort: Int = 7000
) {
    private val TAG = "UdpTunForwarder"

    // Cache de sessões ativas: Chave = "srcPort:destIp:destPort" -> DatagramChannel
    private val sessions = ConcurrentHashMap<String, UdpSession>()

    data class UdpSession(
        val channel: DatagramChannel,
        val clientIp: ByteArray,
        val clientPort: Int,
        val serverIp: ByteArray,
        val serverPort: Int,
        val isUsingRelay: Boolean,
        var lastActive: Long = System.currentTimeMillis()
    )

    /**
     * Processa um pacote IP lido da interface TUN.
     * Retorna o número de bytes transmitidos.
     */
    fun handlePacket(packet: ByteArray, length: Int): Int {
        if (length < 20) return 0

        val version = (packet[0].toInt() shr 4) and 0x0F
        if (version != 4) return 0 // Apenas IPv4 suportado

        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (length < ihl) return 0

        val totalLength = ((packet[2].toInt() and 0xFF) shl 8) or (packet[3].toInt() and 0xFF)
        if (totalLength < ihl || totalLength > length) return 0
        // Fragmented IPv4 packets are not reassembled by this experimental forwarder.
        val fragmentField = ((packet[6].toInt() and 0xFF) shl 8) or (packet[7].toInt() and 0xFF)
        if ((fragmentField and 0x3FFF) != 0) return 0

        val protocol = packet[9].toInt() and 0xFF
        val srcIp = packet.copyOfRange(12, 16)
        val dstIp = packet.copyOfRange(16, 20)

        return when (protocol) {
            1 -> handleIcmp(packet, length, ihl, srcIp, dstIp)
            17 -> handleUdp(packet, totalLength, ihl, srcIp, dstIp)
            // TCP, IPv6 and other protocols are intentionally rejected. This is not a full VPN.
            else -> 0
        }
    }

    private fun handleIcmp(packet: ByteArray, length: Int, ihl: Int, srcIp: ByteArray, dstIp: ByteArray): Int {
        if (length < ihl + 8) return 0
        val type = packet[ihl].toInt() and 0xFF
        val code = packet[ihl + 1].toInt() and 0xFF

        // Responde a Echo Request (Ping) localmente para diagnóstico
        if (type == 8 && code == 0) {
            val reply = packet.copyOf(length)
            reply[ihl] = 0.toByte() // Echo Reply

            System.arraycopy(dstIp, 0, reply, 12, 4)
            System.arraycopy(srcIp, 0, reply, 16, 4)

            reply[ihl + 2] = 0
            reply[ihl + 3] = 0
            val icmpChecksum = computeChecksum(reply, ihl, length - ihl)
            reply[ihl + 2] = ((icmpChecksum shr 8) and 0xFF).toByte()
            reply[ihl + 3] = (icmpChecksum and 0xFF).toByte()

            reply[10] = 0
            reply[11] = 0
            val ipChecksum = computeChecksum(reply, 0, ihl)
            reply[10] = ((ipChecksum shr 8) and 0xFF).toByte()
            reply[11] = (ipChecksum and 0xFF).toByte()

            synchronized(tunOutput) {
                try {
                    tunOutput.write(reply, 0, length)
                    tunOutput.flush()
                } catch (_: Exception) {}
            }
            return length
        }
        return 0
    }

    private fun handleUdp(packet: ByteArray, length: Int, ihl: Int, srcIp: ByteArray, dstIp: ByteArray): Int {
        if (length < ihl + 8) return 0

        val srcPort = ((packet[ihl].toInt() and 0xFF) shl 8) or (packet[ihl + 1].toInt() and 0xFF)
        val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
        val udpLen = ((packet[ihl + 4].toInt() and 0xFF) shl 8) or (packet[ihl + 5].toInt() and 0xFF)

        if (length < ihl + udpLen || udpLen < 8) return 0
        val payloadLen = udpLen - 8
        val payloadOffset = ihl + 8

        val dstHost = InetAddress.getByAddress(dstIp)
        val sessionKey = "$srcPort:${dstHost.hostAddress}:$dstPort"

        var session = sessions[sessionKey]
        val shouldUseRelay = !relayHost.isNullOrBlank() && relayPort > 0

        if (session == null || !session.channel.isOpen) {
            try {
                val channel = DatagramChannel.open()
                channel.configureBlocking(false)
                
                // PROTEÇÃO NATIVA: Impede que o tráfego do socket reentre na TUN
                vpnService.protect(channel.socket())

                if (shouldUseRelay) {
                    channel.connect(InetSocketAddress(relayHost, relayPort))
                } else {
                    channel.connect(InetSocketAddress(dstHost, dstPort))
                }

                session = UdpSession(
                    channel = channel,
                    clientIp = srcIp,
                    clientPort = srcPort,
                    serverIp = dstIp,
                    serverPort = dstPort,
                    isUsingRelay = shouldUseRelay
                )
                sessions[sessionKey] = session

                startUdpReceiver(sessionKey, session)
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao abrir canal UDP protegido: ${e.message}")
                return 0
            }
        }

        session.lastActive = System.currentTimeMillis()

        try {
            if (session.isUsingRelay) {
                // Empacota com cabeçalho de relay para o nó intermediário:
                // [0x01][0x02][4 bytes IP destino][2 bytes Porta destino][Payload do jogo]
                val relayHeader = ByteArray(8)
                relayHeader[0] = 0x01
                relayHeader[1] = 0x02
                System.arraycopy(dstIp, 0, relayHeader, 2, 4)
                relayHeader[6] = ((dstPort shr 8) and 0xFF).toByte()
                relayHeader[7] = (dstPort and 0xFF).toByte()

                val relayPacket = ByteBuffer.allocate(8 + payloadLen)
                relayPacket.put(relayHeader)
                relayPacket.put(packet, payloadOffset, payloadLen)
                relayPacket.flip()

                session.channel.write(relayPacket)
            } else {
                // Modo Direto: envia o payload puro do jogo ao destino
                val byteBuffer = ByteBuffer.wrap(packet, payloadOffset, payloadLen)
                session.channel.write(byteBuffer)
            }
            return payloadLen
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar datagrama UDP: ${e.message}")
            return 0
        }
    }

    private fun startUdpReceiver(sessionKey: String, session: UdpSession) {
        scope.launch(Dispatchers.IO) {
            val receiveBuffer = ByteBuffer.allocate(32767)
            try {
                while (scope.isActive && session.channel.isOpen) {
                    receiveBuffer.clear()
                    val bytesRead = session.channel.read(receiveBuffer)
                    if (bytesRead > 0) {
                        receiveBuffer.flip()

                        val (effectiveSrcIp, effectiveSrcPort, payload) = if (session.isUsingRelay) {
                            // Desempacota resposta do nó de relay:
                            // Formato: [0x01][0x03][4 bytes IP Origem][2 bytes Porta Origem][Payload]
                            if (bytesRead >= 8 && receiveBuffer.get(0) == 0x01.toByte() && receiveBuffer.get(1) == 0x03.toByte()) {
                                val originIp = ByteArray(4)
                                receiveBuffer.position(2)
                                receiveBuffer.get(originIp)
                                val originPort = ((receiveBuffer.get().toInt() and 0xFF) shl 8) or (receiveBuffer.get().toInt() and 0xFF)
                                val remaining = bytesRead - 8
                                val gamePayload = ByteArray(remaining)
                                receiveBuffer.get(gamePayload)
                                Triple(originIp, originPort, gamePayload)
                            } else {
                                continue
                            }
                        } else {
                            val rawPayload = ByteArray(bytesRead)
                            receiveBuffer.get(rawPayload)
                            Triple(session.serverIp, session.serverPort, rawPayload)
                        }

                        // Constrói o pacote de resposta IPv4 + UDP e injeta na interface TUN
                        val replyPacket = buildIpv4UdpPacket(
                            srcIp = effectiveSrcIp,
                            dstIp = session.clientIp,
                            srcPort = effectiveSrcPort,
                            dstPort = session.clientPort,
                            payload = payload
                        )

                        synchronized(tunOutput) {
                            tunOutput.write(replyPacket)
                            tunOutput.flush()
                        }
                    } else {
                        kotlinx.coroutines.delay(10)
                    }

                    if (System.currentTimeMillis() - session.lastActive > 60_000) {
                        break
                    }
                }
            } catch (_: Exception) {
            } finally {
                try {
                    session.channel.close()
                } catch (_: Exception) {}
                sessions.remove(sessionKey)
            }
        }
    }

    private fun buildIpv4UdpPacket(
        srcIp: ByteArray,
        dstIp: ByteArray,
        srcPort: Int,
        dstPort: Int,
        payload: ByteArray
    ): ByteArray {
        val ipHeaderLen = 20
        val udpHeaderLen = 8
        val totalLen = ipHeaderLen + udpHeaderLen + payload.size
        val packet = ByteArray(totalLen)

        // Cabeçalho IPv4
        packet[0] = 0x45.toByte()
        packet[1] = 0
        packet[2] = ((totalLen shr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        packet[4] = 0
        packet[5] = 0
        packet[6] = 0x40.toByte() // Don't Fragment
        packet[7] = 0
        packet[8] = 64
        packet[9] = 17 // UDP

        System.arraycopy(srcIp, 0, packet, 12, 4)
        System.arraycopy(dstIp, 0, packet, 16, 4)

        val ipChecksum = computeChecksum(packet, 0, ipHeaderLen)
        packet[10] = ((ipChecksum shr 8) and 0xFF).toByte()
        packet[11] = (ipChecksum and 0xFF).toByte()

        // Cabeçalho UDP
        val udpOffset = ipHeaderLen
        packet[udpOffset] = ((srcPort shr 8) and 0xFF).toByte()
        packet[udpOffset + 1] = (srcPort and 0xFF).toByte()
        packet[udpOffset + 2] = ((dstPort shr 8) and 0xFF).toByte()
        packet[udpOffset + 3] = (dstPort and 0xFF).toByte()
        val udpLen = udpHeaderLen + payload.size
        packet[udpOffset + 4] = ((udpLen shr 8) and 0xFF).toByte()
        packet[udpOffset + 5] = (udpLen and 0xFF).toByte()
        packet[udpOffset + 6] = 0
        packet[udpOffset + 7] = 0

        System.arraycopy(payload, 0, packet, udpOffset + udpHeaderLen, payload.size)
        return packet
    }

    private fun computeChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var i = offset
        val end = offset + length - 1

        while (i < end) {
            val b1 = data[i].toInt() and 0xFF
            val b2 = data[i + 1].toInt() and 0xFF
            sum += (b1 shl 8) or b2
            i += 2
        }

        if (i <= offset + length - 1) {
            val b1 = data[i].toInt() and 0xFF
            sum += b1 shl 8
        }

        while ((sum shr 16) > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }

        return (sum.inv()) and 0xFFFF
    }

    fun close() {
        sessions.values.forEach { session ->
            try {
                session.channel.close()
            } catch (_: Exception) {}
        }
        sessions.clear()
    }
}
