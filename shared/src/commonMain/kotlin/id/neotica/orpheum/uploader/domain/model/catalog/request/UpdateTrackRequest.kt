package id.neotica.orpheum.uploader.domain.model.catalog.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTrackRequest(
    val title: String? = null,
    @SerialName("track_number") val trackNumber: Int? = null
)
