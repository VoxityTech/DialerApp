package voxity.org.dialer

import android.app.Application
import voxity.org.dialer.managers.CallManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CallManager.getInstance(this)
    }
}