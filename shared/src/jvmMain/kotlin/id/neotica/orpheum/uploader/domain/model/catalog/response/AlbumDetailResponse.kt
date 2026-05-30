package id.neotica.orpheum.uploader.domain.model.catalog.response

import id.neotica.orpheum.uploader.domain.model.catalog.request.AlbumRemoteModel
import kotlinx.serialization.Serializable

@Serializable
data class AlbumDetailResponse(
    val album: AlbumRemoteModel,
    val tracks: List<TrackRemoteModel>
)