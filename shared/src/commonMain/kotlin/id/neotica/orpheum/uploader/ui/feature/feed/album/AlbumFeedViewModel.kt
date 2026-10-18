package id.neotica.orpheum.uploader.ui.feature.feed.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumFeedViewModel(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumFeedUiState())
    val state: StateFlow<AlbumFeedUiState> = _state.asStateFlow()

    init {
        loadAlbums()
    }

    private fun loadAlbums() = loadPage(page = 1, append = false)

    fun refresh() {
        if (_state.value.isLoading) return
        loadPage(page = 1, append = false)
    }

    fun loadMore() {
        val currentState = _state.value
        if (!currentState.hasMore || currentState.isLoading) return
        loadPage(page = currentState.currentPage + 1, append = true)
    }

    private fun loadPage(page: Int, append: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            catalogRepository.getAlbums(page = page)
                .onSuccess { response ->
                    _state.update {
                        val combined = if (append) it.albums + response.data else response.data
                        it.copy(
                            albums = combined,
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
}
