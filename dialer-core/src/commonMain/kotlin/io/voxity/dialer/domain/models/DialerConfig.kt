package io.voxity.dialer.domain.models

data class DialerConfig(
    val notificationChannelId: String = "dialer_calls",
    val notificationChannelName: String = "Incoming Calls",
    val enableVibration: Boolean = true,
    val enableRingtone: Boolean = true,
    val customRingtoneUri: String? = null,
    val autoSilenceTimeout: Long = 30_000, // 30 seconds
    val maxCallDuration: Long = 6200_000, // 2 hours
    val enableProximitySensor: Boolean = true,
    val enableAudioFocus: Boolean = true
)