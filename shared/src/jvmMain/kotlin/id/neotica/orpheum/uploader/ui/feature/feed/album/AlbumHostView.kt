package id.neotica.orpheum.uploader.ui.feature.feed.album

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import id.neotica.orpheum.uploader.ui.feature.albumdetail.AlbumDetailView

@Composable
fun AlbumHostView() {
    // If null, we are on the Feed. If it holds an ID, we are on the Detail screen.
    var selectedAlbumId by remember { mutableStateOf<String?>(null) }

    if (selectedAlbumId == null) {
        AlbumFeedView(
            onAlbumClick = { clickedId ->
                selectedAlbumId = clickedId // Triggers navigation to Detail
            }
        )
    } else {
        AlbumDetailView(
            albumId = selectedAlbumId ?: "",
            onNavigateBack = {
                selectedAlbumId = null // Triggers navigation back to Feed
            }
        )
    }
}