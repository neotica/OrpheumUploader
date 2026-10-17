package id.neotica.orpheum.uploader.domain.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistFeedResponse(
    val data: List<PlaylistRemoteModel>,
    val page: Int,
    val limit: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int
)
