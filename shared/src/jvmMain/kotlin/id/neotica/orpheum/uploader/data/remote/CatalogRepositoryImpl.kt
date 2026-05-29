package id.neotica.orpheum.uploader.data.remote

import id.neotica.orpheum.uploader.domain.model.catalog.*
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.File

class CatalogRepositoryImpl(
    private val httpClient: HttpClient
): CatalogRepository {
    private val baseUrl = "$BASE_URL/orpheum"

    override suspend fun createArtist(name: String, bio: String?, imageUrl: String?): Result<String> = try {
        val response = httpClient.post("$baseUrl/admin/artists") {
            contentType(ContentType.Application.Json)
            setBody(ArtistRequest(name, bio, imageUrl))
        }
        if (response.status.isSuccess()) {
            val body = response.body<CreationResponse>()
            Result.success(body.id)
        } else {
            Result.failure(Exception("Failed to create artist: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createAlbum(artistId: String, title: String, releaseYear: Int, coverUrl: String?): Result<String> = try {
        val response = httpClient.post("$baseUrl/admin/albums") {
            contentType(ContentType.Application.Json)
            setBody(AlbumRequest(artistId, title, releaseYear, coverUrl))
        }
        if (response.status.isSuccess()) {
            val body = response.body<CreationResponse>()
            Result.success(body.id)
        } else {
            Result.failure(Exception("Failed to create album: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadTrack(
        albumId: String,
        title: String,
        trackNumber: Int,
        durationSeconds: Int,
        file: File,
        onProgress: (Float) -> Unit
    ): Result<String> = try {
        val response = httpClient.post("$baseUrl/admin/tracks") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("album_id", albumId)
                        append("title", title)
                        append("track_number", trackNumber.toString())
                        append("duration_seconds", durationSeconds.toString())

                        append("file", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "audio/mpeg")
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file\"; filename=\"${file.name}\""
                            )
                        })
                    }
                )
            )

            onUpload { bytesSent, totalBytes ->
                if (totalBytes != null && totalBytes > 0) {
                    onProgress(bytesSent.toFloat() / totalBytes.toFloat())
                }
            }
        }

        if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
            Result.success(response.bodyAsText())
        } else {
            val errorBody = response.bodyAsText()
            Result.failure(Exception("Track upload failed: ${response.status} - $errorBody"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}