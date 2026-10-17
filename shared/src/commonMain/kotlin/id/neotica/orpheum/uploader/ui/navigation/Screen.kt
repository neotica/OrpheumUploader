package id.neotica.orpheum.uploader.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable data object Upload : Screen
    @Serializable data object Search : Screen
    @Serializable data object Feed : Screen
    @Serializable data object Albums : Screen
    @Serializable data class AlbumDetail(val albumId: String) : Screen
    @Serializable data object Playlists : Screen
    @Serializable data class PlaylistDetail(val slug: String) : Screen
    @Serializable data object Account : Screen
}
