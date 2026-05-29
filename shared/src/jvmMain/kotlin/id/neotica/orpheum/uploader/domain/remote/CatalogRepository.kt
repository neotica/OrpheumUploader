package id.neotica.orpheum.uploader.domain.remote

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
}