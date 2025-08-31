package io.voxity.dialer

import android.app.Application
import io.voxity.dialer.di.androidCoreModule
import io.voxity.dialer.ui.DialerUI
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DialerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DialerApplication)
            modules(
                androidCoreModule + DialerUI.commonUiModule
            )
        }
    }
}