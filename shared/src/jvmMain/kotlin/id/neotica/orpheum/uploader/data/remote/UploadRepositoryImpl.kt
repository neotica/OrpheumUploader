package id.neotica.orpheum.uploader.data.remote

import id.neotica.orpheum.uploader.domain.remote.UploadRepository
import id.neotica.orpheum.uploader.utils.Constants.BASE_URL_BUCKET
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.io.File

class UploadRepositoryImpl(
    private val httpClient: HttpClient
) : UploadRepository {

    override suspend fun uploadAlbumCover(
        file: File,
        albumId: String
    ): Result<String> {
        return try {
            // Target the 'orpheum' bucket specifically
            val targetUrl = "$BASE_URL_BUCKET/orpheum/upload/form"

            val response = httpClient.post(targetUrl) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", file.readBytes(), Headers.build {
                                // Dynamically assign mime type so the browser can read it later
                                val mimeType = if (file.extension.equals("png", true)) {
                                    "image/png"
                                } else "image/jpeg"

                                append(HttpHeaders.ContentType, mimeType)

                                // Route the file into the specific album's folder
                                val fileName = "albums/$albumId/cover.${file.extension.lowercase()}"
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"file\"; filename=\"${fileName}\""
                                )
                            })
                        }
                    )
                )
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Album cover upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}