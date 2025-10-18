package dev.mos.prom

import android.app.Application
import dev.mos.prom.di.modules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MosPromApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MosPromApplication)
            modules(modules = modules)
        }
    }

}
