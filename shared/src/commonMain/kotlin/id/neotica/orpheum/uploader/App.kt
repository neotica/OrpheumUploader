package id.neotica.orpheum.uploader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import id.neotica.orpheum.uploader.platform.installPlatformKeyDispatcher
import id.neotica.orpheum.uploader.ui.MainView
import id.neotica.orpheum.uploader.ui.feature.albumdetail.AlbumDetailView
import id.neotica.orpheum.uploader.ui.feature.auth.AuthView
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackOverlay
import id.neotica.orpheum.uploader.ui.feature.playlist.PlaylistDetailView
import id.neotica.orpheum.uploader.ui.navigation.Screen
import id.neotica.orpheum.uploader.ui.navigation.navItems
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val tokenStorage = koinInject<TokenStorage>()
    var hasToken by remember { mutableStateOf(tokenStorage.getToken() != null) }

    if (!hasToken) {
        AuthView(
            onLoginSuccess = { hasToken = true }
        )
        return
    }

    val tabScreens = remember { navItems.map { it.screen } }
    val backStack = remember { NavBackStack<Screen>(Screen.Upload) }

    val logout = {
        tokenStorage.clearToken()
        hasToken = false
    }

    DisposableEffect(Unit) {
        val handle = installPlatformKeyDispatcher { event ->
            val digit = event.char?.digitToIntOrNull() ?: -1
            when {
                digit in 1..tabScreens.size && (event.isMetaDown || event.isCtrlDown) -> {
                    backStack.clear()
                    backStack.add(tabScreens[digit - 1])
                    true
                }

                event.isEscape && (
                    backStack.lastOrNull() is Screen.AlbumDetail ||
                        backStack.lastOrNull() is Screen.PlaylistDetail
                    ) -> {
                    backStack.removeLastOrNull()
                    true
                }

                else -> false
            }
        }
        onDispose { handle() }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = { screen ->
            NavEntry(key = screen) { key ->
                when (key) {
                    is Screen.AlbumDetail -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AlbumDetailView(
                                albumId = key.albumId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                            PlaybackOverlay(modifier = Modifier.align(Alignment.BottomCenter))
                        }
                    }

                    is Screen.PlaylistDetail -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            PlaylistDetailView(
                                slug = key.slug,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                            PlaybackOverlay(modifier = Modifier.align(Alignment.BottomCenter))
                        }
                    }

                    else -> MainView(
                        screen = key,
                        onNavigateTab = { target ->
                            backStack.clear()
                            backStack.add(target)
                        },
                        onAlbumClick = { albumId ->
                            backStack.add(Screen.AlbumDetail(albumId))
                        },
                        onPlaylistClick = { slug ->
                            backStack.add(Screen.PlaylistDetail(slug))
                        },
                        onLogout = logout,
                    )
                }
            }
        }
    )
}
