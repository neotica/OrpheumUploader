package id.neotica.orpheum.uploader.domain.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistRemoteModel(
    val id: String,
    val name: String,
    val bio: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
