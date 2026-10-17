package id.neotica.orpheum.uploader.domain.model.playlist

import id.neotica.orpheum.uploader.domain.model.catalog.response.TrackRemoteModel
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistWithTracksResponse(
    val playlist: PlaylistRemoteModel,
    val tracks: List<TrackRemoteModel>
)
