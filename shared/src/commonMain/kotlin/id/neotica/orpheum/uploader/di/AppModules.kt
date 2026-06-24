package id.neotica.orpheum.uploader.di

import id.neotica.orpheum.uploader.data.remote.AuthRepositoryImpl
import id.neotica.orpheum.uploader.data.remote.CatalogRepositoryImpl
import id.neotica.orpheum.uploader.data.remote.UploadRepositoryImpl
import id.neotica.orpheum.uploader.domain.remote.AuthRepository
import id.neotica.orpheum.uploader.domain.remote.CatalogRepository
import id.neotica.orpheum.uploader.domain.remote.UploadRepository
import id.neotica.orpheum.uploader.ui.feature.albumdetail.AlbumDetailViewModel
import id.neotica.orpheum.uploader.ui.feature.auth.LoginViewModel
import id.neotica.orpheum.uploader.ui.feature.feed.TrackFeedViewModel
import id.neotica.orpheum.uploader.ui.feature.feed.album.AlbumFeedViewModel
import id.neotica.orpheum.uploader.ui.feature.playback.PlaybackViewModel
import id.neotica.orpheum.uploader.ui.feature.search.SearchViewModel
import id.neotica.orpheum.uploader.ui.feature.upload.UploadViewModel
import id.neotica.orpheum.uploader.utils.Constants.BASE_URL
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::CatalogRepositoryImpl).bind(CatalogRepository::class)
    singleOf(::UploadRepositoryImpl).bind(UploadRepository::class)

    single<AuthRepository> {
        AuthRepositoryImpl(
            get(),
            get(),
            BASE_URL
        )
    }

    singleOf(::PlaybackViewModel)

    viewModelOf(::LoginViewModel)
    viewModelOf(::UploadViewModel)
    viewModelOf(::TrackFeedViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::AlbumFeedViewModel)
    viewModelOf(::AlbumDetailViewModel)
}

expect val platformModule: Module

fun initializeKoin() = startKoin {
    modules(sharedModule, platformModule)
}
