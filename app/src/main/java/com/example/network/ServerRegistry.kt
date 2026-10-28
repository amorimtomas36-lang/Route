package com.example.network

import com.example.model.ServerNode

/**
 * Do not ship public websites as if they were RouteBoost VPN relays.
 *
 * A WireGuard endpoint must run WireGuard and have a matching client config.
 * The experimental UDP relay must run RouteBoost's relay protocol on its
 * configured UDP port. Ordinary websites and cloud provider domains do neither.
 *
 * Keep this list empty until real, tested RouteBoost nodes are provisioned.
 * Users can add a server they control via the app's custom-server flow.
 */
object ServerRegistry {

    val defaultServers: List<ServerNode> = emptyList()

    fun getAllServers(customServers: List<ServerNode> = emptyList()): List<ServerNode> {
        return (defaultServers + customServers).distinctBy { it.id }
    }
}
