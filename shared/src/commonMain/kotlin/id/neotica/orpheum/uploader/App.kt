package id.neotica.orpheum.uploader

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import id.neotica.orpheum.uploader.ui.MainView
import id.neotica.orpheum.uploader.ui.feature.auth.AuthView
import id.neotica.orpheum.uploader.ui.feature.upload.PlatformUploadView
import id.neotica.orpheum.uploader.ui.navigation.Screen
import org.koin.compose.koinInject

@Composable
@Preview
fun App(tokenStorage: TokenStorage = koinInject()) {
    val startScreen: Screen = if (tokenStorage.getToken() != null) Screen.Main else Screen.Auth
    val backStack = remember { NavBackStack<Screen>(startScreen) }

    NavDisplay(
        backStack = backStack,
        entryProvider = { screen ->
            NavEntry(key = screen) { key ->
                when (key) {
                    Screen.Main -> MainView(
                        onLogout = {
                            tokenStorage.clearToken()
                            backStack.clear()
                            backStack.add(Screen.Auth)
                        },
                        uploadView = { PlatformUploadView() }
                    )
                    Screen.RegisterApp -> {}
                    else -> AuthView(
                        { backStack.add(Screen.Main) }
                    )
                }
            }
        }
    )
}
