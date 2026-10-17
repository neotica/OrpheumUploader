package id.neotica.orpheum.uploader.domain.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistRemoteModel(
    val id: String,
    @SerialName("user_id") val userId: String,
    val slug: String,
    val title: String,
    val description: String? = null,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
