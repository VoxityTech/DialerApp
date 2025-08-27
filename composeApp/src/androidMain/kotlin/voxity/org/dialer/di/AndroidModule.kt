// src/androidMain/kotlin/voxity/org/dialer/di/AndroidModule.kt
package voxity.org.dialer.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voxity.org.dialer.domain.repository.CallRepository
import voxity.org.dialer.managers.CallManager

val androidModule = module {
    single<CallRepository> { CallManager.getInstance(androidContext()) }
}