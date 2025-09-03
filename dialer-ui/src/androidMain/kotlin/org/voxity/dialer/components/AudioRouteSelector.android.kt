package org.voxity.dialer.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.voxity.dialer.audio.AudioRoute

@Composable
actual fun PlatformAudioRouteWidgets(
    onRouteSelected: () -> Unit,
    modifier: Modifier
) {
    val routeHandler = remember {
        { route: AudioRoute ->
            Log.d("AudioRoute", "Selected route: ${route.displayName}")
            onRouteSelected()
        }
    }

    AudioRouteWidgetRow(
        onRouteSelected = routeHandler,
        modifier = modifier
    )
}