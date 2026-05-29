package id.neotica.orpheum.uploader.ui.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(
    private val repository: CatalogRepository
): ViewModel() {
    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    // --- Input Handlers ---
    fun updateArtistName(name: String) = _state.update { it.copy(artistName = name) }
    fun updateArtistBio(bio: String) = _state.update { it.copy(artistBio = bio) }
    fun updateAlbumTitle(title: String) = _state.update { it.copy(albumTitle = title) }
    fun updateReleaseYear(year: String) = _state.update { it.copy(releaseYear = year) }
    fun updateTrackTitle(title: String) = _state.update { it.copy(trackTitle = title) }
    fun updateTrackNumber(number: String) = _state.update { it.copy(trackNumber = number) }

    fun setFile(file: File?) = _state.update {
        it.copy(
            selectedFile = file,
            statusMessage = file?.let { f -> "Selected: ${f.name}" } ?: "File removed."
        )
    }

    fun dismissMessage() = _state.update { it.copy(statusMessage = null) }

    // --- Core Logic ---
    fun submitUpload() {
        val currentState = _state.value

        // Basic Validation
        if (
            currentState.artistName.isBlank() ||
            currentState.albumTitle.isBlank() ||
            currentState.trackTitle.isBlank() ||
            currentState.selectedFile == null
        ) {
            _state.update { it.copy(statusMessage = "Please fill in all required fields and select a file.") }
            return
        }

        val releaseYearInt = currentState.releaseYear.toIntOrNull() ?: 2024
        val trackNumberInt = currentState.trackNumber.toIntOrNull() ?: 1
        // Mocking duration to 0 for now as specified; we can add ID3 extraction later
        val durationSecondsInt = 0

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, statusMessage = "Creating Artist...", uploadProgress = 0f, isSuccess = false)
            }

            // Step 1: Create Artist
            val artistId = repository.createArtist(
                name = currentState.artistName,
                bio = currentState.artistBio.ifBlank { null },
                imageUrl = null // Placeholder for future feature
            ).getOrElse { error ->
                _state.update { it.copy(isLoading = false, statusMessage = "Artist Error: ${error.message}") }
                return@launch
            }

            _state.update { it.copy(statusMessage = "Creating Album...") }

            // Step 2: Create Album
            val albumId = repository.createAlbum(
                artistId = artistId,
                title = currentState.albumTitle,
                releaseYear = releaseYearInt,
                coverUrl = null // Placeholder for future feature
            ).getOrElse { error ->
                _state.update { it.copy(isLoading = false, statusMessage = "Album Error: ${error.message}") }
                return@launch
            }

            _state.update { it.copy(statusMessage = "Uploading Audio File...") }

            // Step 3: Upload Track (Multipart)
            repository.uploadTrack(
                albumId = albumId,
                title = currentState.trackTitle,
                trackNumber = trackNumberInt,
                durationSeconds = durationSecondsInt,
                file = currentState.selectedFile,
                onProgress = { progress ->
                    _state.update { it.copy(uploadProgress = progress) }
                }
            ).onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        statusMessage = "Success! Track uploaded securely.",
                        // Reset form fields but keep artist/album to allow rapid uploading of a 2nd track
                        trackTitle = "",
                        trackNumber = (trackNumberInt + 1).toString(),
                        selectedFile = null,
                        uploadProgress = 0f
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, statusMessage = "Upload Error: ${error.message}") }
            }
        }
    }
}