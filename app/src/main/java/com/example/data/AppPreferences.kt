package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.GameCategory
import com.example.model.GameModel
import com.example.model.ServerNode
import com.example.model.ServerProtocol
import com.example.model.ServerRegion
import com.example.model.TestHistoryItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gestor de persistência local para preferências de rota, jogos personalizados,
 * servidores customizados e histórico de benchmarks de latência.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("routeboost_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_GAME_ID = "selected_game_id"
        private const val KEY_SELECTED_SERVER_ID = "selected_server_id"
        private const val KEY_AUTO_ROUTE = "auto_route_enabled"
        private const val KEY_SPLIT_TUNNEL = "split_tunnel_enabled"
        private const val KEY_MTU = "mtu_size"
        private const val KEY_DNS = "gaming_dns"
        private const val KEY_CUSTOM_GAMES = "custom_games_json"
        private const val KEY_CUSTOM_SERVERS = "custom_servers_json"
        private const val KEY_TEST_HISTORY = "test_history_json"
    }

    var selectedGameId: String
        get() = prefs.getString(KEY_SELECTED_GAME_ID, "free_fire") ?: "free_fire"
        set(value) = prefs.edit().putString(KEY_SELECTED_GAME_ID, value).apply()

    var selectedServerId: String
        get() = prefs.getString(KEY_SELECTED_SERVER_ID, "br_sp_01") ?: "br_sp_01"
        set(value) = prefs.edit().putString(KEY_SELECTED_SERVER_ID, value).apply()

    var isAutoRouteEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_ROUTE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_ROUTE, value).apply()

    var isSplitTunnelEnabled: Boolean
        get() = prefs.getBoolean(KEY_SPLIT_TUNNEL, true)
        set(value) = prefs.edit().putBoolean(KEY_SPLIT_TUNNEL, value).apply()

    var mtuSize: Int
        get() = prefs.getInt(KEY_MTU, 1420)
        set(value) = prefs.edit().putInt(KEY_MTU, value).apply()

    var gamingDns: String
        get() = prefs.getString(KEY_DNS, "1.1.1.1") ?: "1.1.1.1"
        set(value) = prefs.edit().putString(KEY_DNS, value).apply()

    // Jogos personalizados
    fun getCustomGames(): List<GameModel> {
        val raw = prefs.getString(KEY_CUSTOM_GAMES, "[]") ?: "[]"
        val list = mutableListOf<GameModel>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val targetArray = obj.optJSONArray("targetHosts")
                val hosts = mutableListOf<String>()
                if (targetArray != null) {
                    for (h in 0 until targetArray.length()) {
                        hosts.add(targetArray.getString(h))
                    }
                }
                list.add(
                    GameModel(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        packageName = obj.getString("packageName"),
                        publisher = obj.optString("publisher", "Personalizado"),
                        category = GameCategory.OUTROS,
                        targetHosts = if (hosts.isNotEmpty()) hosts else listOf(obj.optString("targetHost", "1.1.1.1")),
                        defaultPort = obj.optInt("defaultPort", 443),
                        iconEmoji = obj.optString("iconEmoji", "🕹️"),
                        isInstalled = false
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveCustomGame(game: GameModel) {
        val current = getCustomGames().toMutableList()
        current.removeAll { it.id == game.id }
        current.add(0, game)
        val array = JSONArray()
        for (g in current) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            obj.put("packageName", g.packageName)
            obj.put("publisher", g.publisher)
            obj.put("targetHost", g.targetHosts.firstOrNull() ?: "")
            val hostsArr = JSONArray()
            g.targetHosts.forEach { hostsArr.put(it) }
            obj.put("targetHosts", hostsArr)
            obj.put("defaultPort", g.defaultPort)
            obj.put("iconEmoji", g.iconEmoji)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_GAMES, array.toString()).apply()
    }

    // Servidores personalizados
    fun getCustomServers(): List<ServerNode> {
        val raw = prefs.getString(KEY_CUSTOM_SERVERS, "[]") ?: "[]"
        val list = mutableListOf<ServerNode>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ServerNode(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        city = obj.optString("city", "Meu Servidor"),
                        country = obj.optString("country", "VPS Dedicado"),
                        region = ServerRegion.CUSTOM,
                        host = obj.getString("host"),
                        port = obj.optInt("port", 51820),
                        protocol = ServerProtocol.WIREGUARD,
                        flagEmoji = "⚡",
                        isCustom = true
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun saveCustomServer(server: ServerNode) {
        val current = getCustomServers().toMutableList()
        current.removeAll { it.id == server.id }
        current.add(0, server)
        val array = JSONArray()
        for (s in current) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("city", s.city)
            obj.put("country", s.country)
            obj.put("host", s.host)
            obj.put("port", s.port)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOM_SERVERS, array.toString()).apply()
    }

    // Histórico de testes
    fun getTestHistory(): List<TestHistoryItem> {
        val raw = prefs.getString(KEY_TEST_HISTORY, "[]") ?: "[]"
        val list = mutableListOf<TestHistoryItem>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TestHistoryItem(
                        id = obj.getString("id"),
                        gameName = obj.getString("gameName"),
                        serverName = obj.getString("serverName"),
                        directPingMs = obj.getInt("directPingMs"),
                        boostedPingMs = obj.getInt("boostedPingMs"),
                        directJitterMs = obj.getInt("directJitterMs"),
                        boostedJitterMs = obj.getInt("boostedJitterMs"),
                        packetLossPercent = obj.getDouble("packetLossPercent").toFloat(),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        improved = obj.getBoolean("improved")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun addTestHistoryItem(item: TestHistoryItem) {
        val current = getTestHistory().toMutableList()
        current.add(0, item)
        // Mantém os últimos 30 registos
        val trimmed = current.take(30)
        val array = JSONArray()
        for (h in trimmed) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("gameName", h.gameName)
            obj.put("serverName", h.serverName)
            obj.put("directPingMs", h.directPingMs)
            obj.put("boostedPingMs", h.boostedPingMs)
            obj.put("directJitterMs", h.directJitterMs)
            obj.put("boostedJitterMs", h.boostedJitterMs)
            obj.put("packetLossPercent", h.packetLossPercent.toDouble())
            obj.put("timestamp", h.timestamp)
            obj.put("improved", h.improved)
            array.put(obj)
        }
        prefs.edit().putString(KEY_TEST_HISTORY, array.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().putString(KEY_TEST_HISTORY, "[]").apply()
    }
}
