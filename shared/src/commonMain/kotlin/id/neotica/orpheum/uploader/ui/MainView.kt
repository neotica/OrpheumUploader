package id.neotica.orpheum.uploader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.ui.feature.account.AccountView
import id.neotica.orpheum.uploader.ui.feature.feed.TrackFeedView
import id.neotica.orpheum.uploader.ui.feature.feed.album.AlbumFeedView
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackOverlay
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import id.neotica.orpheum.uploader.ui.feature.playlist.PlaylistsView
import id.neotica.orpheum.uploader.ui.feature.search.SearchView
import id.neotica.orpheum.uploader.ui.feature.upload.PlatformUploadView
import id.neotica.orpheum.uploader.ui.navigation.AppNavigationBar
import id.neotica.orpheum.uploader.ui.navigation.AppNavigationRail
import id.neotica.orpheum.uploader.ui.navigation.Screen
import org.koin.compose.koinInject

@Composable
fun MainView(
    screen: Screen,
    onNavigateTab: (Screen) -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    MaterialTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxWidth < 600.dp

            if (isCompact) {
                Scaffold(
                    topBar = { MainTopBar() },
                    bottomBar = {
                        AppNavigationBar(
                            currentScreen = screen,
                            onNavigate = onNavigateTab,
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(paddingValues)
                    ) {
                        MainContent(screen, onAlbumClick, onPlaylistClick, onLogout)
                        PlaybackOverlay(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            } else {
                Scaffold(
                    topBar = { MainTopBar() }
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
                                onNavigate = onNavigateTab,
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .background(DarkBackground)
                            ) {
                                MainContent(screen, onAlbumClick, onPlaylistClick, onLogout)
                            }
                        }

                        PlaybackOverlay(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTopBar() {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = "Orpheum Uploader",
                    color = DarkPrimary,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
        )
        HorizontalDivider(Modifier, thickness = 2.dp, color = DarkPrimary)
    }
}

@Composable
private fun MainContent(
    screen: Screen,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val playbackViewModel = koinInject<PlaybackViewModel>()

    when (screen) {
        Screen.Upload -> PlatformUploadView()
        Screen.Search -> SearchView(playbackViewModel = playbackViewModel)
        Screen.Feed -> TrackFeedView(playbackViewModel = playbackViewModel)
        Screen.Albums -> AlbumFeedView(onAlbumClick = onAlbumClick)
        Screen.Playlists -> PlaylistsView(onPlaylistClick = onPlaylistClick)
        Screen.Account -> AccountView(onLogout = onLogout)
        else -> {}
    }
}
