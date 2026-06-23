package id.neotica.orpheum.uploader.ui.feature.albumdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun AlbumDetailView(
    albumId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlbumDetailViewModel?
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Album Detail (Android) - coming soon")
    }
}
