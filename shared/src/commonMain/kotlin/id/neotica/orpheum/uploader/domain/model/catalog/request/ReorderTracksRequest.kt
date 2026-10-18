package id.neotica.orpheum.uploader.domain.model.catalog.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReorderTracksRequest(
    @SerialName("track_ids") val trackIds: List<String>
)
