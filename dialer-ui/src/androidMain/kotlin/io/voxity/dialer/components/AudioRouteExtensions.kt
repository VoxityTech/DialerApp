package io.voxity.dialer.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import io.voxity.dialer.audio.AudioRoute

val AudioRoute.icon: ImageVector
    get() = when (this) {
        AudioRoute.SPEAKER -> Icons.Default.VolumeUp
        AudioRoute.EARPIECE -> Icons.Default.Phone
        AudioRoute.BLUETOOTH -> Icons.Default.Bluetooth
        AudioRoute.WIRED_HEADSET -> Icons.Default.Headset
    }