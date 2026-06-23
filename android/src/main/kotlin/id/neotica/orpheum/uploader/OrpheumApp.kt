package id.neotica.orpheum.uploader

import android.app.Application
import id.neotica.orpheum.uploader.di.platformModule
import id.neotica.orpheum.uploader.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OrpheumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OrpheumApp)
            modules(sharedModule, platformModule)
        }
    }
}
