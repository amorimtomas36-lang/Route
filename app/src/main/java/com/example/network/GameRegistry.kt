package com.example.network

import android.content.Context
import android.content.pm.PackageManager
import com.example.model.GameCategory
import com.example.model.GameModel

object GameRegistry {

    private val defaultGames = listOf(
        GameModel(
            id = "free_fire",
            name = "Free Fire",
            packageName = "com.dts.freefireth",
            publisher = "Garena",
            category = GameCategory.BATTLE_ROYALE,
            targetHosts = listOf("sa-east-1.amazonaws.com", "garena.com"),
            defaultPort = 443,
            iconEmoji = "🔥"
        ),
        GameModel(
            id = "free_fire_max",
            name = "Free Fire MAX",
            packageName = "com.dts.freefiremax",
            publisher = "Garena",
            category = GameCategory.BATTLE_ROYALE,
            targetHosts = listOf("sa-east-1.amazonaws.com", "garena.com"),
            defaultPort = 443,
            iconEmoji = "💥"
        ),
        GameModel(
            id = "mlbb",
            name = "Mobile Legends: Bang Bang",
            packageName = "com.mobile.legends",
            publisher = "Moonton",
            category = GameCategory.MOBA,
            targetHosts = listOf("moonton.com", "mobilelegends.com"),
            defaultPort = 443,
            iconEmoji = "⚔️"
        ),
        GameModel(
            id = "wild_rift",
            name = "LoL: Wild Rift",
            packageName = "com.riotgames.league.wildrift",
            publisher = "Riot Games",
            category = GameCategory.MOBA,
            targetHosts = listOf("riotgames.com", "leagueoflegends.com"),
            defaultPort = 443,
            iconEmoji = "🛡️"
        ),
        GameModel(
            id = "pubg_mobile",
            name = "PUBG MOBILE",
            packageName = "com.tencent.ig",
            publisher = "Level Infinite",
            category = GameCategory.BATTLE_ROYALE,
            targetHosts = listOf("pubgmobile.com", "levelinfinite.com"),
            defaultPort = 443,
            iconEmoji = "🪖"
        ),
        GameModel(
            id = "cod_mobile",
            name = "Call of Duty: Mobile",
            packageName = "com.activision.callofduty.shooter",
            publisher = "Activision",
            category = GameCategory.FPS,
            targetHosts = listOf("activision.com", "callofduty.com"),
            defaultPort = 443,
            iconEmoji = "🎯"
        ),
        GameModel(
            id = "roblox",
            name = "Roblox",
            packageName = "com.roblox.client",
            publisher = "Roblox Corp",
            category = GameCategory.OUTROS,
            targetHosts = listOf("roblox.com", "rbxcdn.com"),
            defaultPort = 443,
            iconEmoji = "🧱"
        ),
        GameModel(
            id = "brawl_stars",
            name = "Brawl Stars",
            packageName = "com.supercell.brawlstars",
            publisher = "Supercell",
            category = GameCategory.MOBA,
            targetHosts = listOf("supercell.com", "brawlstars.com"),
            defaultPort = 443,
            iconEmoji = "⭐"
        ),
        GameModel(
            id = "ea_fc_mobile",
            name = "EA SPORTS FC Mobile",
            packageName = "com.ea.gp.fifamobile",
            publisher = "Electronic Arts",
            category = GameCategory.OUTROS,
            targetHosts = listOf("ea.com", "fifa.com"),
            defaultPort = 443,
            iconEmoji = "⚽"
        ),
        GameModel(
            id = "stumble_guys",
            name = "Stumble Guys",
            packageName = "com.kitkagames.fallbuddies",
            publisher = "Scopely",
            category = GameCategory.OUTROS,
            targetHosts = listOf("scopely.com", "stumbleguys.com"),
            defaultPort = 443,
            iconEmoji = "🏃"
        )
    )

    /**
     * Retorna a lista de jogos, identificando quais estão instalados no dispositivo atual.
     */
    fun getGamesWithInstalledStatus(context: Context, customGames: List<GameModel> = emptyList()): List<GameModel> {
        val pm = context.packageManager
        val allGames = (defaultGames + customGames).distinctBy { it.id }

        return allGames.map { game ->
            val isInstalled = try {
                pm.getPackageInfo(game.packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
            game.copy(isInstalled = isInstalled)
        }
    }
}
