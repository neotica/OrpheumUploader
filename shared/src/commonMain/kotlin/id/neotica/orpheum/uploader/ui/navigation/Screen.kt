package id.neotica.orpheum.uploader.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable data object Auth : Screen
    @Serializable data object Main : Screen
    @Serializable data object RegisterApp : Screen
}
