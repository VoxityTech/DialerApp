package io.voxity.dialer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import io.voxity.dialer.audio.AudioRoute
import io.voxity.dialer.audio.AudioRouteManager

private class AudioRouteWrapper(
    private val route: AudioRoute
) : AudioRouteInfo {
    override val displayName: String = route.displayName
    override val icon: ImageVector = when (route) {
        AudioRoute.SPEAKER -> Icons.Default.VolumeUp
        AudioRoute.EARPIECE -> Icons.Default.Phone
        AudioRoute.BLUETOOTH -> Icons.Default.Bluetooth
        AudioRoute.WIRED_HEADSET -> Icons.Default.Headset
    }
}

@Composable
fun AudioRouteWidgetRow(
    onRouteSelected: (AudioRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioRouteManager = remember { AudioRouteManager(context) }

    val availableRoutes by audioRouteManager.availableRoutes.collectAsState()
    val currentRoute by audioRouteManager.currentRoute.collectAsState()

    LaunchedEffect(Unit) {
        audioRouteManager.refreshRoutesForCall()
    }

    val allRoutes = listOf(
        AudioRoute.EARPIECE,
        AudioRoute.SPEAKER,
        AudioRoute.BLUETOOTH,
        AudioRoute.WIRED_HEADSET
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        allRoutes.forEach { route ->
            val isAvailable = availableRoutes.contains(route)
            val isSelected = route == currentRoute && isAvailable

            AudioRouteWidget(
                route = AudioRouteWrapper(route),
                isSelected = isSelected,
                isAvailable = isAvailable,
                onClick = {
                    if (isAvailable) {
                        audioRouteManager.setAudioRoute(route)
                        onRouteSelected(route)
                    }
                }
            )
        }
    }
}