package id.neotica.orpheum.uploader.domain.remote

import java.io.File

interface UploadRepository {
    suspend fun uploadAlbumCover(
        file: File,
        albumId: String
    ): Result<String>
}