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

    private fun loadAlbums() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            catalogRepository.getAlbums()
                .onSuccess { response ->
                    _state.update { it.copy(albums = response.data, isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }
}