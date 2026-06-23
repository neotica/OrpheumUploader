package id.neotica.orpheum.uploader.ui.feature.upload

import java.io.File

data class UploadUiState(
    // Artist Data
    val artistName: String = "",
    val artistBio: String = "",

    // Album Data
    val albumTitle: String = "",
    val releaseYear: String = "",

    // Track & File Data
    val trackTitle: String = "",
    val trackNumber: String = "",
    val selectedFile: File? = null,

    // Upload Status
    val isLoading: Boolean = false,
    val uploadProgress: Float = 0f,
    val statusMessage: String? = null,
    val isSuccess: Boolean = false
)