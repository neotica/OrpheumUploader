package id.neotica.orpheum.uploader.domain.model.catalog.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumRequest(
    @SerialName("artist_id")
    val artistId: String,
    val title: String,
    @SerialName("release_year")
    val releaseYear: Int,
    @SerialName("cover_url")
    val coverUrl: String?
)