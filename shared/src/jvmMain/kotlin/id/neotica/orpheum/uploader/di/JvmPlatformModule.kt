package id.neotica.orpheum.uploader.di

import id.neotica.orpheum.uploader.data.local.DesktopAudioPlayer
import id.neotica.orpheum.uploader.data.local.DesktopTokenStorage
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module = module {
    singleOf(::DesktopTokenStorage).bind(TokenStorage::class)
    single<AudioPlayer> { DesktopAudioPlayer() }
    single<HttpClient> {
        val storage = get<TokenStorage>()
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true })
            }
            install(Logging) {
                logger = Logger.DEFAULT; level = LogLevel.HEADERS
            }
            install(Auth) {
                bearer {
                    loadTokens { storage.getToken()?.let { BearerTokens(it, "") } }
                    sendWithoutRequest { true }
                }
            }
        }
    }
}
