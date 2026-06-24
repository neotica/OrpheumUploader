package id.neotica.orpheum.uploader.ui.feature.search

import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel

data class SearchUiState(
    val query: String = "",
    val results: List<TrackRemoteModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = false,
)
