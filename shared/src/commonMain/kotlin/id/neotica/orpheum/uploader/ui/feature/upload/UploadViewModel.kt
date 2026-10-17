package id.neotica.orpheum.uploader.ui.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.orpheum.uploader.domain.model.catalog.request.UpdateAlbumRequest
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.domain.remote.UploadRepository
import id.neotica.orpheum.uploader.platform.readAudioDurationSeconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(
    private val repository: CatalogRepository,
    private val uploadRepository: UploadRepository
): ViewModel() {
    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    private val bucketName = "orpheum"

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

    fun setCoverFile(file: File?) = _state.update {
        it.copy(
            selectedCoverFile = file,
            statusMessage = file?.let { f -> "Cover selected: ${f.name}" } ?: "Cover removed."
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
        val audioFile = currentState.selectedFile
        val coverFile = currentState.selectedCoverFile

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, statusMessage = "Creating Artist...", uploadProgress = 0f, isSuccess = false)
            }

            // Step 1: Create Artist
            val artistId = repository.createArtist(
                name = currentState.artistName,
                bio = currentState.artistBio.ifBlank { null },
                imageUrl = null
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
                coverUrl = null
            ).getOrElse { error ->
                _state.update { it.copy(isLoading = false, statusMessage = "Album Error: ${error.message}") }
                return@launch
            }

            // Step 3: Upload Cover Art (optional, non-fatal on failure)
            var coverWarning: String? = null
            if (coverFile != null) {
                _state.update { it.copy(statusMessage = "Uploading Cover Art...") }

                val coverPath = uploadRepository.uploadAlbumCover(coverFile, albumId)
                    .map {
                        "$bucketName/albums/$albumId/cover.${coverFile.extension.lowercase()}"
                    }
                    .getOrElse { error ->
                        coverWarning = "Cover upload failed: ${error.message}"
                        null
                    }

                if (coverPath != null) {
                    repository.updateAlbum(albumId, UpdateAlbumRequest(coverUrl = coverPath))
                        .onFailure { error ->
                            coverWarning = "Cover attach failed: ${error.message}"
                        }
                }
            }

            // Step 4: Extract real track duration from the audio file
            _state.update { it.copy(statusMessage = "Reading Audio Metadata...") }
            val durationSecondsInt = readAudioDurationSeconds(audioFile) ?: 0

            _state.update { it.copy(statusMessage = "Uploading Audio File...") }

            // Step 5: Upload Track (Multipart)
            repository.uploadTrack(
                albumId = albumId,
                title = currentState.trackTitle,
                trackNumber = trackNumberInt,
                durationSeconds = durationSecondsInt,
                file = audioFile,
                onProgress = { progress ->
                    _state.update { it.copy(uploadProgress = progress) }
                }
            ).onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        statusMessage = buildString {
                            append("Success! Track uploaded securely.")
                            if (coverWarning != null) append("\n$coverWarning")
                        },
                        // Reset form fields but keep artist/album to allow rapid uploading of a 2nd track
                        trackTitle = "",
                        trackNumber = (trackNumberInt + 1).toString(),
                        selectedFile = null,
                        selectedCoverFile = null,
                        uploadProgress = 0f
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, statusMessage = "Upload Error: ${error.message}") }
            }
        }
    }
}
