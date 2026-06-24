package id.neotica.orpheum.uploader.di

import id.neotica.orpheum.uploader.data.local.AndroidAudioPlayer
import id.neotica.orpheum.uploader.data.local.AndroidMediaSessionController
import id.neotica.orpheum.uploader.data.local.AndroidTokenStorage
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import id.neotica.orpheum.uploader.domain.local.MediaSessionController
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<TokenStorage> { AndroidTokenStorage(get()) }
    single<AudioPlayer> { AndroidAudioPlayer() }
    single<MediaSessionController> { AndroidMediaSessionController(get()) }
    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true })
            }
            install(Logging) {
                logger = Logger.DEFAULT; level = LogLevel.HEADERS
            }
            install(Auth) {
                bearer {
                    loadTokens { get<TokenStorage>().getToken()?.let { BearerTokens(it, "") } }
                    sendWithoutRequest { true }
                }
            }
        }
    }
}
