package io.voxity.dialer

import android.content.Context
import io.voxity.dialer.di.androidModule
import io.voxity.dialer.di.commonModule
import org.koin.core.module.Module

object DialerCore {
    fun initialize(context: Context) {
        initializeDialerCore(context)
    }
    val coreModules: List<Module> get() = listOf(
        commonModule
    ) + platformModules
}

actual fun initializeDialerCore(context: Any) {
    require(context is Context) { "Expected Android Context" }
    initializeContext(context)
}

actual val platformModules: List<Module> = listOf(androidModule)