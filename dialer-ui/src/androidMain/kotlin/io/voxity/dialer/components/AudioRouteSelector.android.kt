package io.voxity.dialer.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformAudioRouteWidgets(
    onRouteSelected: () -> Unit,
    modifier: Modifier
) {
    val routeHandler = remember {
        { route: io.voxity.dialer.audio.AudioRoute ->
            android.util.Log.d("AudioRoute", "Selected route: ${route.displayName}")
            onRouteSelected()
        }
    }

    AudioRouteWidgetRow(
        onRouteSelected = routeHandler,
        modifier = modifier
    )
}