package id.neotica.orpheum.uploader.ui.feature.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.ui.components.DarkBackground
import id.neotica.orpheum.uploader.ui.components.DarkPrimary
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrackFeedView(
    playbackViewModel: PlaybackViewModel,
    viewModel: TrackFeedViewModel = koinViewModel()
) {
    val feedState by viewModel.state.collectAsState()
    val currentTrack by playbackViewModel.currentTrack.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "New Releases",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = DarkPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (feedState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp)
            )
        } else if (feedState.errorMessage != null) {
            Text(
                "Error: ${feedState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(feedState.tracks) { track ->
                    val isActive = track.id == currentTrack?.id

                    TrackCompactItem(
                        track = track,
                        isActive = isActive,
                        onClick = { viewModel.playTrack(track) }
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

@Composable
fun TrackCompactItem(
    track: TrackRemoteModel,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isActive) DarkPrimary.copy(alpha = 0.12f)
                else DarkBackground
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) DarkPrimary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artistName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isActive) {
            Text(
                text = "\u266B",
                style = MaterialTheme.typography.titleMedium,
                color = DarkPrimary,
            )
        }
    }
}

@Preview
@Composable
fun TrackCompactItemInactivePreview() {
    TrackCompactItem(
        track = TrackRemoteModel(
            id = "1",
            albumId = "album-1",
            title = "Bohemian Rhapsody",
            artistName = "Queen",
            durationSeconds = 354,
            fileUrl = "https://example.com/stream/1",
            trackNumber = 1,
        ),
        isActive = false,
        onClick = {},
    )
}

@Preview
@Composable
fun TrackCompactItemActivePreview() {
    TrackCompactItem(
        track = TrackRemoteModel(
            id = "1",
            albumId = "album-1",
            title = "Bohemian Rhapsody",
            artistName = "Queen",
            durationSeconds = 354,
            fileUrl = "https://example.com/stream/1",
            trackNumber = 1,
        ),
        isActive = true,
        onClick = {},
    )
}
