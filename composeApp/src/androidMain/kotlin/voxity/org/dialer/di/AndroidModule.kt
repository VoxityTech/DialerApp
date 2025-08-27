// src/androidMain/kotlin/voxity/org/dialer/di/AndroidModule.kt
package voxity.org.dialer.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import voxity.org.dialer.data.AndroidCallHistoryRepository
import voxity.org.dialer.data.AndroidContactRepository
import voxity.org.dialer.data.CallLogReader
import voxity.org.dialer.data.ContactsReader
import voxity.org.dialer.domain.repository.CallHistoryRepository
import voxity.org.dialer.domain.repository.CallRepository
import voxity.org.dialer.domain.repository.ContactRepository
import voxity.org.dialer.managers.CallManager

val androidModule = module {
    single<CallRepository> { CallManager.getInstance(androidContext()) }
    single { ContactsReader(androidContext()) }
    single { CallLogReader(androidContext()) }
    single<ContactRepository> { AndroidContactRepository(get()) }
    single<CallHistoryRepository> { AndroidCallHistoryRepository(get()) }
}