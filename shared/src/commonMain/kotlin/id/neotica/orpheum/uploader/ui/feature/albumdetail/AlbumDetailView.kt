package id.neotica.orpheum.uploader.ui.feature.albumdetail

import androidx.compose.runtime.Composable

@Composable
expect fun AlbumDetailView(
    albumId: String,
    onNavigateBack: () -> Unit,
    viewModel: AlbumDetailViewModel? = null
)
