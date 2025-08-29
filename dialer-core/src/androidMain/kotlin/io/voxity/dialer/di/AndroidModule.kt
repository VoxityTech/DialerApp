package io.voxity.dialer.di

import io.voxity.dialer.audio.CallAudioManager
import io.voxity.dialer.audio.CallRingtoneManager
import io.voxity.dialer.audio.VolumeKeyHandler
import io.voxity.dialer.blocking.ContactBlockManager
import io.voxity.dialer.data.AndroidCallHistoryRepository
import io.voxity.dialer.data.AndroidContactRepository
import io.voxity.dialer.data.CallLogReader
import io.voxity.dialer.data.ContactsReader
import io.voxity.dialer.managers.CallManager
import io.voxity.dialer.notifications.CallNotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import io.voxity.dialer.domain.repository.CallHistoryRepository
import io.voxity.dialer.domain.repository.CallRepository
import io.voxity.dialer.domain.repository.ContactRepository
import io.voxity.dialer.domain.interfaces.AudioController
import io.voxity.dialer.platform.PhoneCaller
import io.voxity.dialer.sensors.ProximitySensorManager
import kotlinx.coroutines.launch

val androidModule = module {
    // Core dependencies
    single { ContactsReader(androidContext()) }
    single { CallLogReader(androidContext()) }
    single<ContactRepository> { AndroidContactRepository(get()) }
    single<CallHistoryRepository> { AndroidCallHistoryRepository(get()) }

    // Audio management
    single<AudioController> { CallAudioManager(androidContext()) }
    single { CallAudioManager(androidContext()) }
    single { CallRingtoneManager(androidContext()) }

    // Block management
    single { ContactBlockManager(androidContext()) }

    // Notifications
    single { CallNotificationManager(androidContext(), get()) }

    // Call management - now with proper DI
    single<CallRepository> {
        CallManager(
            context = androidContext(),
            config = get(),
            audioManager = get<CallAudioManager>(),
            ringtoneManager = get(),
            notificationManager = get(),
            blockManager = get()
        )
    }

    // Platform caller
    single { PhoneCaller(androidContext()) }

    // Sensors
    single { ProximitySensorManager(androidContext()) }

    // Volume handler with proper DI - fix method names
    single {
        VolumeKeyHandler(
            onVolumeUp = {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    get<CallAudioManager>().increaseVolume()
                }
            },
            onVolumeDown = {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    get<CallAudioManager>().decreaseVolume()
                }
            },
            onSilenceRingtone = { get<CallRingtoneManager>().silenceRinging() }
        )
    }
}