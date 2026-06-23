package id.neotica.orpheum.uploader.ui.feature.playback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary

@Composable
fun MiniPlayerBar(
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by playbackViewModel.currentTrack.collectAsState()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val progress by playbackViewModel.playbackProgress.collectAsState()

    MiniPlayerBarContent(
        currentTrack = currentTrack,
        isPlaying = isPlaying,
        progress = progress,
        onPlayPauseClick = {
            if (isPlaying) playbackViewModel.pause() else playbackViewModel.resume()
        },
        onStopClick = { playbackViewModel.stop() },
        onSeek = { fraction ->
            currentTrack?.let { track ->
                val seconds = (track.durationSeconds * fraction).toInt()
                    .coerceIn(0, track.durationSeconds)
                playbackViewModel.seekTo(seconds)
            }
        },
        modifier = modifier
    )
}

@Composable
fun MiniPlayerBarContent(
    currentTrack: TrackRemoteModel?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentTrack == null) return

    var sliderValue by remember { mutableStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }

    if (!isDragging) {
        LaunchedEffect(progress) {
            sliderValue = progress
        }
    }

    val displayProgress = if (isDragging) sliderValue else progress

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        shadowElevation = 8.dp,
        color = DarkBackground
    ) {
        Column {
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it; isDragging = true },
                onValueChangeFinished = { isDragging = false; onSeek(sliderValue) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = DarkPrimary,
                    activeTrackColor = DarkPrimary,
                    inactiveTrackColor = DarkPrimary.copy(alpha = 0.3f),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration((currentTrack.durationSeconds * displayProgress).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatDuration(currentTrack.durationSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = currentTrack.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\u23F9",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.clickable(onClick = onStopClick).padding(8.dp)
                    )

                    val icon = if (isPlaying) "\u23F8" else "\u25B6\uFE0F"
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.clickable(onClick = onPlayPauseClick).padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Preview
@Composable
private fun MiniPlayerBarPreview() {
    MiniPlayerBarContent(
        currentTrack = TrackRemoteModel(
            id = "1",
            albumId = "album-1",
            title = "Bohemian Rhapsody",
            artistName = "Queen",
            durationSeconds = 354,
            fileUrl = "https://example.com/stream/1",
            trackNumber = 1,
        ),
        isPlaying = true,
        progress = 0.45f,
        onPlayPauseClick = {},
        onStopClick = {},
        onSeek = {},
    )
}
