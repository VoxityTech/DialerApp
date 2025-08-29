package io.voxity.dialer

import android.app.Application
import io.voxity.dialer.domain.models.DialerConfig
import io.voxity.dialer.ui.DialerUI
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DialerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = DialerConfig(
            notificationChannelId = "dialer_calls",
            notificationChannelName = "Dialer Calls",
            enableVibration = true,
            enableRingtone = true
        )

        // Initialize dialer core
        val initResult = DialerCoreManager.initialize(this, config)
        if (initResult is io.voxity.dialer.domain.models.CallResult.Error) {
            // Handle initialization error
            android.util.Log.e("DialerApp", "Failed to initialize dialer core: ${initResult.message}")
        }
    }
}