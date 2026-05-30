package id.neotica.orpheum.uploader.di

import id.neotica.orpheum.uploader.data.local.DesktopAudioPlayer
import id.neotica.orpheum.uploader.data.local.DesktopTokenStorage
import id.neotica.orpheum.uploader.data.remote.AuthRepositoryImpl
import id.neotica.orpheum.uploader.data.remote.CatalogRepositoryImpl
import id.neotica.orpheum.uploader.data.remote.UploadRepositoryImpl
import id.neotica.orpheum.uploader.domain.local.AudioPlayer
import id.neotica.orpheum.uploader.domain.local.TokenStorage
import id.neotica.orpheum.uploader.domain.remote.AuthRepository
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.domain.remote.UploadRepository
import id.neotica.orpheum.uploader.ui.feature.albumdetail.AlbumDetailViewModel
import id.neotica.orpheum.uploader.ui.feature.auth.LoginViewModel
import id.neotica.orpheum.uploader.ui.feature.feed.TrackFeedViewModel
import id.neotica.orpheum.uploader.ui.feature.feed.album.AlbumFeedViewModel
import id.neotica.orpheum.uploader.ui.feature.upload.UploadViewModel
import id.neotica.orpheum.uploader.utils.Constants.BASE_URL
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
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModules = module {
    singleOf(::DesktopTokenStorage).bind(TokenStorage::class)
    single<AudioPlayer> { DesktopAudioPlayer() }

    single<AuthRepository> {
        AuthRepositoryImpl(
            get(),
            get(),
            BASE_URL
        )
    }

    singleOf(::CatalogRepositoryImpl).bind(CatalogRepository::class)
    singleOf(::UploadRepositoryImpl).bind(UploadRepository::class)

    viewModelOf(::LoginViewModel)
    viewModelOf(::UploadViewModel)
    viewModelOf(::TrackFeedViewModel)
    viewModelOf(::AlbumFeedViewModel)
    viewModelOf(::AlbumDetailViewModel)
}
val networkModule = module {
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
                    loadTokens {
                        storage.getToken()?.let { BearerTokens(it, "") }
                    }
                    sendWithoutRequest { true }
                }
            }
        }
    }
}

val appModules = arrayOf(networkModule, dataModules)

fun initializeKoin() = startKoin { modules(*appModules) }