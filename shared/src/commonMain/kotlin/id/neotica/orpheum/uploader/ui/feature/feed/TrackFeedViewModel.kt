package id.neotica.orpheum.uploader.ui.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackFeedViewModel(
    private val repository: CatalogRepository,
    private val playbackViewModel: PlaybackViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    init {
        loadFeed()
    }

    private fun loadFeed() = loadPage(page = 1, append = false)

    fun loadMore() {
        val currentState = _state.value
        if (!currentState.hasMore || currentState.isLoading) return
        loadPage(page = currentState.currentPage + 1, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getNewReleases(page = page)
                .onSuccess { response ->
                    _state.update {
                        val combined = if (append) it.tracks + response.data else response.data
                        it.copy(
                            tracks = combined,
                            isLoading = false,
                            currentPage = response.page,
                            totalPages = response.totalPages,
                            hasMore = response.page < response.totalPages
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun playTrack(track: TrackRemoteModel) {
        playbackViewModel.play(track)
    }
}
