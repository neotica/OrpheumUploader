package id.neotica.orpheum.uploader.ui.feature.albumdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.model.catalog.request.UpdateAlbumRequest
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.domain.remote.UploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class AlbumDetailViewModel(
    private val albumId: String,
    private val catalogRepository: CatalogRepository,
    private val uploadRepository: UploadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumDetailUiState())
    val state: StateFlow<AlbumDetailUiState> = _state.asStateFlow()

    private val bucketName = "orpheum"

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            catalogRepository.getAlbumDetails(albumId)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            albumDetails = response,
                            isLoading = false,
                            // Pre-fill the edit fields
                            editTitle = response.album.title,
                            editYear = response.album.releaseYear.toString()
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    fun updateTitle(newTitle: String) {
        _state.update { it.copy(editTitle = newTitle) }
    }

    fun updateYear(newYear: String) {
        _state.update { it.copy(editYear = newYear) }
    }

    fun setCoverFile(file: File?) {
        _state.update { it.copy(selectedCoverFile = file) }
    }

    fun saveChanges() {
        val currentState = _state.value
        if (currentState.albumDetails == null) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }

            var finalCoverPath: String? = null

            // 1. Upload Cover Art (if a new one was selected)
            if (currentState.selectedCoverFile != null) {
                val file = currentState.selectedCoverFile
                val extension = file.extension.lowercase()

                uploadRepository.uploadAlbumCover(file, albumId)
                    .onSuccess {
                        // Success! SeaweedFS has the file.
                        // Now construct the path that the DB expects:
                        finalCoverPath = "$bucketName/albums/$albumId/cover.$extension"
                    }
                    .onFailure { error ->
                        _state.update { it.copy(isSaving = false, errorMessage = "Cover Upload Failed: ${error.message}") }
                        return@launch // Abort the whole save process if image fails
                    }
            }

            // 2. Update Database via Ktor Admin Endpoint
            val request = UpdateAlbumRequest(
                title = currentState.editTitle.takeIf { it != currentState.albumDetails.album.title },
                releaseYear = currentState.editYear.toIntOrNull()
                    ?.takeIf { it != currentState.albumDetails.album.releaseYear },
                coverUrl = finalCoverPath // Null if no new image was uploaded
            )

            // Optimization: Don't send a PUT request if nothing actually changed
            if (request.title == null && request.releaseYear == null && request.coverUrl == null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            catalogRepository.updateAlbum(albumId, request)
                .onSuccess { updatedAlbum ->
                    // Re-load the details to get the fresh data from the server
                    loadDetails()
                    _state.update {
                        it.copy(
                            isSaving = false,
                            selectedCoverFile = null, // Clear the dropped file
                            errorMessage = "Saved Successfully!"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, errorMessage = "DB Update Failed: ${error.message}") }
                }
        }
    }

    fun deleteAlbum() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            catalogRepository.deleteAlbum(albumId)
                .onSuccess {
                    _state.update { it.copy(isSaving = false, isDeleted = true) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, errorMessage = "Delete Failed: ${error.message}") }
                }
        }
    }
}