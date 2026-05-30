package id.neotica.orpheum.uploader.ui.feature.albumdetail

import id.neotica.orpheum.uploader.domain.model.catalog.response.AlbumDetailResponse
import java.io.File

data class AlbumDetailUiState(
    val albumDetails: AlbumDetailResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,

    // Edit Form State
    val editTitle: String = "",
    val editYear: String = "",
    val selectedCoverFile: File? = null,

    val isSaving: Boolean = false,
    val isDeleted: Boolean = false
)