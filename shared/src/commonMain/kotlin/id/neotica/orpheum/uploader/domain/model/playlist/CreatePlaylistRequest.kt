package id.neotica.orpheum.uploader.domain.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistRequest(
    val title: String,
    val description: String? = null,
    @SerialName("is_public") val isPublic: Boolean? = null
)
