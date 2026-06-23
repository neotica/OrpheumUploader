package id.neotica.orpheum.uploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.ui.feature.account.AccountView
import id.neotica.orpheum.uploader.ui.feature.albumdetail.AlbumDetailView
import id.neotica.orpheum.uploader.ui.feature.auth.AuthView
import id.neotica.orpheum.uploader.ui.feature.feed.TrackFeedView
import id.neotica.orpheum.uploader.ui.feature.feed.album.AlbumFeedView
import id.neotica.orpheum.uploader.ui.feature.playback.MiniPlayerBar
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import id.neotica.orpheum.uploader.ui.feature.upload.PlatformUploadView
import id.neotica.orpheum.uploader.ui.navigation.AppNavigationRail
import id.neotica.orpheum.uploader.ui.navigation.Screen
import org.koin.compose.koinInject

@Composable
fun MainView() {
    val tokenStorage = koinInject<TokenStorage>()
    val startScreen: Screen = if (tokenStorage.getToken() != null) Screen.Upload else Screen.Auth
    val backStack = remember { NavBackStack<Screen>(startScreen) }

    NavDisplay(
        backStack = backStack,
        entryProvider = { screen ->
            NavEntry(key = screen) { key ->
                when (key) {
                    Screen.Auth -> AuthView(
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Screen.Upload)
                        }
                    )
                    Screen.RegisterApp -> {}
                    is Screen.AlbumDetail -> AlbumDetailView(
                        albumId = key.albumId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                    else -> MainShell(
                        screen = key,
                        backStackSize = backStack.size,
                        onNavigate = { target ->
                            backStack.clear()
                            backStack.add(target)
                        },
                        onAlbumClick = { albumId ->
                            backStack.add(Screen.AlbumDetail(albumId))
                        },
                        onLogout = {
                            tokenStorage.clearToken()
                            backStack.clear()
                            backStack.add(Screen.Auth)
                        },
                    )
                }
            }
        }
    )
}

@Composable
private fun MainShell(
    screen: Screen,
    backStackSize: Int,
    onNavigate: (Screen) -> Unit,
    onAlbumClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val playbackViewModel = koinInject<PlaybackViewModel>()

    MaterialTheme {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (screen) {
                                    is Screen.Feed -> "Track Feed"
                                    is Screen.Albums -> "Album Manager"
                                    is Screen.Account -> "Account"
                                    else -> "Orpheum Uploader"
                                },
                                color = DarkPrimary,
                            )
                        },
                        navigationIcon = {
                            if (backStackSize > 1) {
                                IconButton(onClick = { /* handled by NavDisplay's onBack */ }) {
                                    Text("\u2B05\uFE0F", color = DarkPrimary)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = DarkBackground
                        )
                    )
                    HorizontalDivider(Modifier, thickness = 2.dp, color = DarkPrimary)
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigationRail(
                        currentScreen = screen,
                        onNavigate = onNavigate,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(DarkBackground)
                    ) {
                        when (screen) {
                            Screen.Upload -> PlatformUploadView()
                            Screen.Feed -> TrackFeedView(playbackViewModel = playbackViewModel)
                            Screen.Albums -> AlbumFeedView(onAlbumClick = onAlbumClick)
                            Screen.Account -> AccountView(onLogout = onLogout)
                            else -> {}
                        }
                    }
                }

                MiniPlayerBar(
                    playbackViewModel = playbackViewModel,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
