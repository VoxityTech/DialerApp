package org.voxity.dialer.di

import org.voxity.dialer.audio.CallAudioManager
import org.voxity.dialer.audio.CallRecordingManager
import org.voxity.dialer.audio.CallRingtoneManager
import org.voxity.dialer.audio.VolumeKeyHandler
import org.voxity.dialer.blocking.ContactBlockManager
import org.voxity.dialer.data.AndroidCallHistoryRepository
import org.voxity.dialer.data.AndroidContactRepository
import org.voxity.dialer.data.CallLogReader
import org.voxity.dialer.data.ContactsReader
import org.voxity.dialer.managers.CallManager
import org.voxity.dialer.notifications.CallNotificationManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.voxity.dialer.domain.repository.CallHistoryRepository
import org.voxity.dialer.domain.repository.CallRepository
import org.voxity.dialer.domain.repository.ContactRepository
import org.voxity.dialer.domain.interfaces.AudioController
import org.voxity.dialer.domain.models.DialerConfig
import org.voxity.dialer.domain.usecases.CallUseCases
import org.voxity.dialer.platform.PhoneCaller
import org.voxity.dialer.sensors.ProximitySensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val androidCoreModule = module {

    single {
        DialerConfig(
            notificationChannelId = "dialer_calls",
            notificationChannelName = "Dialer Calls",
            enableVibration = true,
            enableRingtone = true
        )
    }
    // Core dependencies
    single { ContactsReader(androidContext()) }
    single { CallLogReader(androidContext()) }
    single<ContactRepository> { AndroidContactRepository(get()) }
    single<CallHistoryRepository> { AndroidCallHistoryRepository(get()) }
    single { ProximitySensorManager(androidContext()) }
    single { CallRecordingManager(androidContext()) }

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

    // Also provide CallManager directly
    single { get<CallRepository>() as CallManager }

    single {
        CallUseCases(
            callRepository = get(),
            contactRepository = get(),
            callHistoryRepository = get()
        )
    }

    // Platform caller
    single { PhoneCaller(androidContext()) }

    single { ProximitySensorManager(androidContext()) }

    single {
        VolumeKeyHandler(
            onVolumeUp = {
                CoroutineScope(Dispatchers.Main).launch {
                    get<CallAudioManager>().increaseVolume()
                }
            },
            onVolumeDown = {
                CoroutineScope(Dispatchers.Main).launch {
                    get<CallAudioManager>().decreaseVolume()
                }
            },
            onSilenceRingtone = { get<CallRingtoneManager>().silenceRinging() }
        )
    }
}