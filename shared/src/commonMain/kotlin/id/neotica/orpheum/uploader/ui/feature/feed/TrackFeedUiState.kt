package id.neotica.orpheum.uploader.ui.feature.feed

import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel

data class FeedUiState(
    val tracks: List<TrackRemoteModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
