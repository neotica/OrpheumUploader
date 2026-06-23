package id.neotica.orpheum.uploader.domain.remote

import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel
import id.neotica.orpheum.uploader.domain.model.catalog.request.UpdateAlbumRequest
import id.neotica.orpheum.uploader.domain.model.catalog.response.AlbumDetailResponse
import id.neotica.orpheum.uploader.domain.model.catalog.response.AlbumFeedResponse
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackFeedResponse
import java.io.File

interface CatalogRepository {
    suspend fun createArtist(
        name: String,
        bio: String?,
        imageUrl: String?
    ): Result<String> // Returns Artist UUID

    suspend fun createAlbum(
        artistId: String,
        title: String,
        releaseYear: Int,
        coverUrl: String?
    ): Result<String> // Returns Album UUID

    suspend fun uploadTrack(
        albumId: String,
        title: String,
        trackNumber: Int,
        durationSeconds: Int,
        file: File,
        onProgress: (Float) -> Unit
    ): Result<String> // Registers and pushes tracking entry
    suspend fun getNewReleases(page: Int = 1, limit: Int = 10): Result<TrackFeedResponse>
    suspend fun getAlbums(page: Int = 1, limit: Int = 20): Result<AlbumFeedResponse>
    suspend fun getAlbumDetails(albumId: String): Result<AlbumDetailResponse>
    suspend fun updateAlbum(albumId: String, request: UpdateAlbumRequest): Result<AlbumRemoteModel> // Or just Result<String> depending on what you need
    suspend fun deleteAlbum(albumId: String): Result<String>
}