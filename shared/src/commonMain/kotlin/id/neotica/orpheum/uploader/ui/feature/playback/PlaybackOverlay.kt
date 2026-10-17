package id.neotica.orpheum.uploader.ui.feature.playback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject

@Composable
fun PlaybackOverlay(modifier: Modifier = Modifier) {
    val playbackViewModel = koinInject<PlaybackViewModel>()
    MiniPlayerBar(
        playbackViewModel = playbackViewModel,
        modifier = modifier,
    )
}
