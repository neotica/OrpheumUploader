package id.neotica.orpheum.uploader.domain.model.catalog.response

import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumFeedResponse(
    val data: List<AlbumRemoteModel>,
    val page: Int,
    val limit: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int
)