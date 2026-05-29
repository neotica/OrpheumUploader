package id.neotica.orpheum.uploader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import id.neotica.orpheum.uploader.di.initializeKoin
import id.neotica.toast.setComposeWindowProvider

fun main() = application {
    initializeKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "OrpheumUploader",
    ) {
        setComposeWindowProvider {
            window
        }
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                }
                .crossfade(true)
                .build()
        }
        App()
    }
}