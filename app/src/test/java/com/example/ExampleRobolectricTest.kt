package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.model.GameCategory
import com.example.model.GameModel
import com.example.model.RouteMetrics
import com.example.model.ServerNode
import com.example.model.ServerRegion
import com.example.network.GameRegistry
import com.example.network.RouteOptimizerEngine
import com.example.network.ServerRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies RouteBoost app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RouteBoost", appName)
    }

    @Test
    fun `route comparison correctly prefers optimized route when node latency is lower`() {
        val direct = RouteMetrics(
            pingMs = 85,
            minPingMs = 80,
            maxPingMs = 95,
            jitterMs = 12,
            packetLossPercent = 0f,
            samplesCount = 5,
            stabilityScore = 70
        )
        val node = RouteMetrics(
            pingMs = 38,
            minPingMs = 35,
            maxPingMs = 42,
            jitterMs = 2,
            packetLossPercent = 0f,
            samplesCount = 5,
            stabilityScore = 95
        )

        val comparison = RouteOptimizerEngine.compareMetrics(
            direct = direct,
            node = node,
            serverName = "São Paulo - BR 01",
            gameName = "Free Fire"
        )

        assertTrue("Deveria indicar que a rota otimizada é melhor", comparison.isOptimizedBetter)
        assertEquals(47, comparison.latencyDiffMs)
        assertEquals(10, comparison.jitterDiffMs)
        assertTrue(comparison.recommendationMessage.contains("Rota otimizada"))
    }

    @Test
    fun `route comparison keeps direct route when direct ping is lower than node`() {
        val direct = RouteMetrics(
            pingMs = 18,
            minPingMs = 16,
            maxPingMs = 20,
            jitterMs = 2,
            packetLossPercent = 0f,
            samplesCount = 5,
            stabilityScore = 98
        )
        val node = RouteMetrics(
            pingMs = 110,
            minPingMs = 105,
            maxPingMs = 120,
            jitterMs = 8,
            packetLossPercent = 0f,
            samplesCount = 5,
            stabilityScore = 75
        )

        val comparison = RouteOptimizerEngine.compareMetrics(
            direct = direct,
            node = node,
            serverName = "Miami - US Gateway",
            gameName = "LoL: Wild Rift"
        )

        assertFalse("Não deve alegar melhoria quando a rota alternativa for pior", comparison.isOptimizedBetter)
        assertEquals(-92, comparison.latencyDiffMs)
        assertTrue(comparison.recommendationMessage.contains("rota direta já é mais rápida"))
    }

    @Test
    fun `preferences stores custom games and servers correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)

        val customGame = GameModel(
            id = "custom_test_game",
            name = "Test FPS",
            packageName = "com.test.fps",
            publisher = "Test Studio",
            category = GameCategory.FPS,
            targetHosts = listOf("test.example.com"),
            defaultPort = 443,
            iconEmoji = "🎯"
        )
        prefs.saveCustomGame(customGame)
        val loadedGames = prefs.getCustomGames()
        assertTrue(loadedGames.any { it.id == "custom_test_game" })

        val customServer = ServerNode(
            id = "custom_test_vps",
            name = "Meu Nó Teste",
            city = "Lisboa",
            country = "Portugal",
            region = ServerRegion.CUSTOM,
            host = "192.0.2.1",
            port = 51820,
            isCustom = true
        )
        prefs.saveCustomServer(customServer)
        val loadedServers = prefs.getCustomServers()
        assertTrue(loadedServers.any { it.id == "custom_test_vps" })
    }

    @Test
    fun `game registry provides default games catalog`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val games = GameRegistry.getGamesWithInstalledStatus(context)
        assertTrue("Deve conter múltiplos jogos padrão", games.size >= 8)
        assertTrue(games.any { it.id == "free_fire" })
        assertTrue(games.any { it.id == "wild_rift" })
    }

    @Test
    fun `server registry provides regional nodes`() {
        val servers = ServerRegistry.getAllServers()
        assertTrue("Deve conter servidores padrão", servers.size >= 6)
        assertTrue(servers.any { it.region == ServerRegion.SOUTH_AMERICA })
        assertTrue(servers.any { it.region == ServerRegion.NORTH_AMERICA })
        assertTrue(servers.any { it.region == ServerRegion.EUROPE })
    }
}
