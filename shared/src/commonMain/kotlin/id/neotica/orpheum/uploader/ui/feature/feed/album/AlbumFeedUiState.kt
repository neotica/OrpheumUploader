package id.neotica.orpheum.uploader.ui.feature.feed.album

import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel

data class AlbumFeedUiState(
    val albums: List<AlbumRemoteModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false
)
