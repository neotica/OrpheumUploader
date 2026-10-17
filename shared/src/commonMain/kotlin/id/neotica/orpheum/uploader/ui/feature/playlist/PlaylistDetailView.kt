package id.neotica.orpheum.uploader.ui.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.feature.feed.TrackCompactItem
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailView(
    slug: String,
    onNavigateBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel(key = slug) { parametersOf(slug) }
) {
    val state by viewModel.state.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.playlist?.title ?: "Playlist",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2B05\uFE0F", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null && state.playlist == null) {
                Text(
                    text = "Error: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    val description = state.playlist?.description
                    if (!description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.tracks.isEmpty()) {
                        Text(
                            text = "No tracks in this playlist yet.",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.tracks, key = { it.id }) { track ->
                                TrackCompactItem(
                                    track = track,
                                    isActive = track.id == currentTrack?.id,
                                    onClick = { viewModel.playTrack(track) },
                                    trailing = {
                                        Text(
                                            text = "\u274C",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .clickable { viewModel.removeTrack(track) }
                                                .padding(8.dp)
                                        )
                                    }
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
