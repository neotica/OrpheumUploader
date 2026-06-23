package id.neotica.orpheum.uploader.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.ui.components.DarkPrimaryCard

@Composable
fun AppNavigationRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        containerColor = DarkPrimaryCard,
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        navItems.forEach { item ->
            NavigationRailItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Text(
                        text = item.indicator,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = DarkPrimary,
                    unselectedIconColor = DarkPrimary.copy(alpha = 0.5f),
                    selectedTextColor = DarkPrimary,
                    unselectedTextColor = DarkPrimary.copy(alpha = 0.5f),
                    indicatorColor = DarkPrimary.copy(alpha = 0.15f),
                ),
            )
        }
    }
}

@Composable
fun AppNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = DarkPrimaryCard,
        tonalElevation = 0.dp,
        modifier = modifier,
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Text(
                        text = item.indicator,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkPrimary,
                    unselectedIconColor = DarkPrimary.copy(alpha = 0.5f),
                    selectedTextColor = DarkPrimary,
                    unselectedTextColor = DarkPrimary.copy(alpha = 0.5f),
                    indicatorColor = DarkPrimary.copy(alpha = 0.15f),
                ),
            )
        }
    }
}
