package io.voxity.dialer.di

import io.voxity.dialer.audio.CallAudioManager
import io.voxity.dialer.audio.CallRingtoneManager
import io.voxity.dialer.audio.VolumeKeyHandler
import io.voxity.dialer.data.AndroidCallHistoryRepository
import io.voxity.dialer.data.AndroidContactRepository
import io.voxity.dialer.data.CallLogReader
import io.voxity.dialer.data.ContactsReader
import io.voxity.dialer.managers.CallManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import io.voxity.dialer.domain.repository.CallHistoryRepository
import io.voxity.dialer.domain.repository.CallRepository
import io.voxity.dialer.domain.repository.ContactRepository
import io.voxity.dialer.platform.PhoneCaller
import io.voxity.dialer.sensors.ProximitySensorManager

val androidModule = module {
    single<CallRepository> { CallManager.Companion.getInstance(androidContext()) }
    single { ContactsReader(androidContext()) }
    single { CallLogReader(androidContext()) }
    single<ContactRepository> { AndroidContactRepository(get()) }
    single<CallHistoryRepository> { AndroidCallHistoryRepository(get()) }

    single { PhoneCaller(androidContext()) }
    single { CallAudioManager(androidContext()) }
    single { ProximitySensorManager(androidContext()) }
    single { CallRingtoneManager(androidContext()) }

    single {
        VolumeKeyHandler(
            onVolumeUp = { get<CallAudioManager>().increaseCallVolume() },
            onVolumeDown = { get<CallAudioManager>().decreaseCallVolume() },
            onSilenceRingtone = { get<CallRingtoneManager>().silenceRinging() }
        )
    }
}