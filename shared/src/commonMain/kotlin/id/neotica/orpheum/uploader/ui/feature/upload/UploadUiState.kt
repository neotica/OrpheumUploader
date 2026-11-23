package id.neotica.orpheum.uploader.ui.feature.upload

import id.neotica.orpheum.uploader.domain.model.artist.ArtistRemoteModel
import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel
import java.io.File

data class UploadUiState(
    // Artist Data
    val artistName: String = "",
    val artistBio: String = "",
    val selectedArtist: ArtistRemoteModel? = null,

    // Artist picker
    val artists: List<ArtistRemoteModel> = emptyList(),
    val artistSearch: String = "",
    val isLoadingArtists: Boolean = false,
    val artistsError: String? = null,

    // Album Data
    val albumTitle: String = "",
    val releaseYear: String = "",
    val selectedCoverFile: File? = null,
    val selectedAlbum: AlbumRemoteModel? = null,

    // Artist albums (for the album dropdown)
    val artistAlbums: List<AlbumRemoteModel> = emptyList(),
    val isLoadingAlbums: Boolean = false,
    val albumsError: String? = null,

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
