package mohaamadreza.saemipour.no.vazheh

import android.app.Application
import mohaamadreza.saemipour.no.vazheh.data.initializeSettings
import mohaamadreza.saemipour.no.vazheh.di.appModule
import mohaamadreza.saemipour.no.vazheh.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NovazhehApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize settings for token storage
        initializeSettings(this)

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@NovazhehApp)
            modules(sharedModule, appModule)
        }
    }
}
