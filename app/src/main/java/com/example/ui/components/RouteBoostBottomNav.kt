package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.NavigationScreen

data class NavItem(
    val screen: NavigationScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun RouteBoostBottomNav(
    currentScreen: NavigationScreen,
    onScreenSelected: (NavigationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(NavigationScreen.HOME, "Início", Icons.Default.Home, "nav_home"),
        NavItem(NavigationScreen.GAMES, "Jogos", Icons.Default.SportsEsports, "nav_games"),
        NavItem(NavigationScreen.SERVERS, "Servidores", Icons.Default.Dns, "nav_servers"),
        NavItem(NavigationScreen.DIAGNOSTICS, "Auditoria", Icons.Default.Analytics, "nav_diagnostics"),
        NavItem(NavigationScreen.SETTINGS, "Ajustes", Icons.Default.Settings, "nav_settings")
    )

    NavigationBar(
        containerColor = DarkSurface,
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkSurface,
                    selectedTextColor = CyanPrimary,
                    indicatorColor = CyanPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
