package com.example.network

import com.example.model.ServerNode
import com.example.model.ServerProtocol
import com.example.model.ServerRegion

object ServerRegistry {

    val defaultServers = listOf(
        // América do Sul
        ServerNode(
            id = "br_sp_01",
            name = "São Paulo - BR 01 (Equinix SP)",
            city = "São Paulo",
            country = "Brasil",
            region = ServerRegion.SOUTH_AMERICA,
            host = "sa-east-1.amazonaws.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇧🇷",
            loadPercent = 32,
            isBenchmarkEndpoint = true
        ),
        ServerNode(
            id = "br_sp_02",
            name = "São Paulo - BR 02 (Ascenty Tier III)",
            city = "São Paulo",
            country = "Brasil",
            region = ServerRegion.SOUTH_AMERICA,
            host = "brmobile.speedtestcustom.com",
            port = 443,
            protocol = ServerProtocol.UDP_RELAY,
            flagEmoji = "🇧🇷",
            loadPercent = 41
        ),
        ServerNode(
            id = "cl_scl_01",
            name = "Santiago - CL 01",
            city = "Santiago",
            country = "Chile",
            region = ServerRegion.SOUTH_AMERICA,
            host = "cl.speedtestcustom.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇨🇱",
            loadPercent = 28
        ),
        ServerNode(
            id = "ar_bue_01",
            name = "Buenos Aires - AR 01",
            city = "Buenos Aires",
            country = "Argentina",
            region = ServerRegion.SOUTH_AMERICA,
            host = "ar.speedtestcustom.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇦🇷",
            loadPercent = 35
        ),

        // América do Norte
        ServerNode(
            id = "us_mia_01",
            name = "Miami - US Gateway (Low Latency LatAm)",
            city = "Miami, FL",
            country = "Estados Unidos",
            region = ServerRegion.NORTH_AMERICA,
            host = "speedtest.miami.fl.us",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇺🇸",
            loadPercent = 48
        ),
        ServerNode(
            id = "us_va_01",
            name = "Virgínia - US East 01 (AWS Ashburn)",
            city = "Ashburn, VA",
            country = "Estados Unidos",
            region = ServerRegion.NORTH_AMERICA,
            host = "us-east-1.amazonaws.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇺🇸",
            loadPercent = 55
        ),
        ServerNode(
            id = "us_ca_01",
            name = "Califórnia - US West 01",
            city = "São Francisco, CA",
            country = "Estados Unidos",
            region = ServerRegion.NORTH_AMERICA,
            host = "us-west-1.amazonaws.com",
            port = 443,
            protocol = ServerProtocol.UDP_RELAY,
            flagEmoji = "🇺🇸",
            loadPercent = 39
        ),

        // Europa
        ServerNode(
            id = "eu_fra_01",
            name = "Frankfurt - EU Central 01",
            city = "Frankfurt",
            country = "Alemanha",
            region = ServerRegion.EUROPE,
            host = "eu-central-1.amazonaws.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇩🇪",
            loadPercent = 44
        ),
        ServerNode(
            id = "eu_lon_01",
            name = "Londres - EU West 01",
            city = "Londres",
            country = "Reino Unido",
            region = ServerRegion.EUROPE,
            host = "eu-west-2.amazonaws.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇬🇧",
            loadPercent = 51
        ),
        ServerNode(
            id = "eu_lis_01",
            name = "Lisboa - PT 01",
            city = "Lisboa",
            country = "Portugal",
            region = ServerRegion.EUROPE,
            host = "cloudflare.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇵🇹",
            loadPercent = 29
        ),
        ServerNode(
            id = "eu_mad_01",
            name = "Madrid - ES 01",
            city = "Madrid",
            country = "Espanha",
            region = ServerRegion.EUROPE,
            host = "google.es",
            port = 443,
            protocol = ServerProtocol.UDP_RELAY,
            flagEmoji = "🇪🇸",
            loadPercent = 33
        ),

        // Ásia
        ServerNode(
            id = "as_sg_01",
            name = "Singapura - AS South 01",
            city = "Singapura",
            country = "Singapura",
            region = ServerRegion.ASIA,
            host = "ap-southeast-1.amazonaws.com",
            port = 443,
            protocol = ServerProtocol.WIREGUARD,
            flagEmoji = "🇸🇬",
            loadPercent = 60
        )
    )

    fun getAllServers(customServers: List<ServerNode> = emptyList()): List<ServerNode> {
        return (defaultServers + customServers).distinctBy { it.id }
    }
}
