package id.neotica.orpheum.uploader.domain.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistTrackRequest(
    @SerialName("track_id") val trackId: String
)
