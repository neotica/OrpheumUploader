package id.neotica.orpheum.uploader.ui.feature.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackFeedView(
    viewModel: TrackFeedViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val playbackProgress by viewModel.audioPlayer.playbackProgress.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text(
            text = "New Releases",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (state.errorMessage != null) {
            Text("Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.tracks) { track ->
                    val isActive = track.id == state.currentlyPlayingId

                    TrackItemCard(
                        track = track,
                        isActive = isActive,
                        isPlaying = isPlaying,
                        progress = if (isActive) playbackProgress else 0f,
                        onPlayClick = {
                            if (isActive && !isPlaying) {
                                viewModel.resumePlayback() // Resume currently paused track
                            } else {
                                viewModel.playTrack(track) // Play a brand new track
                            }
                        },
                        onPauseClick = { viewModel.pausePlayback() }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackItemCard(
    track: TrackRemoteModel,
    isActive: Boolean,
    isPlaying: Boolean,
    progress: Float,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track ${track.trackNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val icon = if (isActive && isPlaying) "⏸" else "▶️"
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.clickable {
                        if (isActive && isPlaying) onPauseClick() else onPlayClick()
                    }.padding(8.dp)
                )
            }

            if (isActive && progress > 0f) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
//                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}