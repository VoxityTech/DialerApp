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
import voxity.org.dialer.platform.PhoneCaller
import voxity.org.dialer.audio.CallAudioManager
import voxity.org.dialer.audio.CallRingtoneManager
import voxity.org.dialer.sensors.ProximitySensorManager
import voxity.org.dialer.audio.VolumeKeyHandler

val androidModule = module {
    single<CallRepository> { CallManager.getInstance(androidContext()) }
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