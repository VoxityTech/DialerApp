package voxity.org.dialer

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import voxity.org.dialer.di.androidModule
import voxity.org.dialer.di.commonModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(commonModule, androidModule)
        }
    }
}