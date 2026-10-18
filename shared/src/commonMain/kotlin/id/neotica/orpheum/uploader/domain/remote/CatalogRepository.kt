package id.neotica.orpheum.uploader.domain.remote

import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel
import id.neotica.orpheum.uploader.domain.model.catalog.request.UpdateAlbumRequest
import id.neotica.orpheum.uploader.domain.model.catalog.response.AlbumDetailResponse
import id.neotica.orpheum.uploader.domain.model.catalog.response.AlbumFeedResponse
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackFeedResponse
import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import id.neotica.orpheum.uploader.domain.model.playlist.PlaylistFeedResponse
import id.neotica.orpheum.uploader.domain.model.playlist.PlaylistRemoteModel
import id.neotica.orpheum.uploader.domain.model.playlist.PlaylistWithTracksResponse
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
    suspend fun updateAlbum(albumId: String, request: UpdateAlbumRequest): Result<AlbumRemoteModel>
    suspend fun deleteAlbum(albumId: String): Result<String>
    suspend fun deleteTrack(trackId: String): Result<String>
    suspend fun updateTrack(trackId: String, title: String?, trackNumber: Int?): Result<TrackRemoteModel>
    suspend fun reorderTracks(albumId: String, trackIds: List<String>): Result<String>
    suspend fun searchTracks(query: String, page: Int = 1, limit: Int = 20): Result<TrackFeedResponse>
    suspend fun getPlaylists(page: Int = 1, limit: Int = 20): Result<PlaylistFeedResponse>
    suspend fun getPlaylist(slug: String): Result<PlaylistWithTracksResponse>
    suspend fun createPlaylist(title: String, description: String?, isPublic: Boolean?): Result<PlaylistRemoteModel>
    suspend fun addTrackToPlaylist(slug: String, trackId: String): Result<String>
    suspend fun removeTrackFromPlaylist(slug: String, trackId: String): Result<String>
}