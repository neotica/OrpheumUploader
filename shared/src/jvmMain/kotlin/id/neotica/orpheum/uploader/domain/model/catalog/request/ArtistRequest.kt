package id.neotica.orpheum.uploader.domain.model.catalog.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistRequest(
    val name: String,
    val bio: String?,
    @SerialName("image_url")
    val imageUrl: String?
)