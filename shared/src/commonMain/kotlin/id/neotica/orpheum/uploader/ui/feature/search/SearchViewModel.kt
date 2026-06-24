package id.neotica.orpheum.uploader.ui.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: CatalogRepository,
    private val playbackViewModel: PlaybackViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _queryInput = MutableStateFlow("")

    init {
        _queryInput
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .filter { it.length >= 2 || it.isEmpty() }
            .onEach { query ->
                if (query.isBlank()) {
                    _state.update { SearchUiState() }
                } else {
                    search(query, page = 1, append = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _queryInput.value = query
        _state.update { it.copy(query = query) }
    }

    fun loadMore() {
        val currentState = _state.value
        if (!currentState.hasMore || currentState.isLoading) return
        search(currentState.query, page = currentState.currentPage + 1, append = true)
    }

    fun playTrack(track: TrackRemoteModel) {
        playbackViewModel.play(track)
    }

    private fun search(query: String, page: Int, append: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            repository.searchTracks(query = query, page = page)
                .onSuccess { response ->
                    _state.update {
                        val combined = if (append) it.results + response.data else response.data
                        it.copy(
                            results = combined,
                            isLoading = false,
                            currentPage = response.page,
                            totalPages = response.totalPages,
                            hasMore = response.page < response.totalPages,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}
