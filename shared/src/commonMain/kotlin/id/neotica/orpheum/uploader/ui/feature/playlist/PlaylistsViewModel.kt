package id.neotica.orpheum.uploader.ui.feature.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistsUiState())
    val state: StateFlow<PlaylistsUiState> = _state.asStateFlow()

    init {
        loadPage(page = 1, append = false)
    }

    fun loadMore() {
        val currentState = _state.value
        if (!currentState.hasMore || currentState.isLoading) return
        loadPage(page = currentState.currentPage + 1, append = true)
    }

    fun createPlaylist(title: String, description: String, isPublic: Boolean) {
        val currentState = _state.value
        if (title.isBlank() || currentState.isCreating) return

        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, createError = null, createSuccess = false) }

            repository.createPlaylist(
                title = title.trim(),
                description = description.ifBlank { null },
                isPublic = isPublic
            )
                .onSuccess {
                    _state.update { it.copy(isCreating = false, createSuccess = true) }
                    loadPage(page = 1, append = false)
                }
                .onFailure { error ->
                    _state.update { it.copy(isCreating = false, createError = error.message) }
                }
        }
    }

    fun consumeCreateResult() = _state.update { it.copy(createSuccess = false, createError = null) }

    private fun loadPage(page: Int, append: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getPlaylists(page = page)
                .onSuccess { response ->
                    _state.update {
                        val combined = if (append) it.playlists + response.data else response.data
                        it.copy(
                            playlists = combined,
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
