package id.neotica.orpheum.uploader.ui.feature.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.utils.resolveMediaUrl

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
    modifier: Modifier = Modifier
) {
    if (currentTrack == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        shadowElevation = 8.dp,
        color = DarkBackground
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = DarkPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val coverUrl = resolveMediaUrl(currentTrack.albumCover)
                if (coverUrl != null) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "\uD83C\uDFB5",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

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
    )
}
