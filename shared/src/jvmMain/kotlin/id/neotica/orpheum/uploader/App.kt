package id.neotica.orpheum.uploader

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import id.neotica.orpheum.uploader.ui.MainView
import id.neotica.orpheum.uploader.ui.feature.auth.AuthView
import id.neotica.orpheum.uploader.ui.navigation.Screen
import org.koin.compose.koinInject

@Composable
@Preview
fun App(tokenStorage: TokenStorage = koinInject()) {
    val startScreen = if (tokenStorage.getToken() != null) Screen.Main else Screen.Auth
    var currentScreen by remember { mutableStateOf(startScreen) }

    Crossfade(currentScreen) {
        when (it) {
            Screen.Main -> MainView {
                tokenStorage.clearToken()
                currentScreen = Screen.Auth
            }
            Screen.RegisterApp -> {}
            else -> AuthView(
                { currentScreen = Screen.Main }
            )
        }
    }
}