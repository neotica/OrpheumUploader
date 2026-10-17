package id.neotica.orpheum.uploader.ui.feature.playlist

import id.neotica.orpheum.uploader.domain.model.playlist.PlaylistRemoteModel

data class PlaylistsUiState(
    val playlists: List<PlaylistRemoteModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
    val isCreating: Boolean = false,
    val createError: String? = null,
    val createSuccess: Boolean = false
)
