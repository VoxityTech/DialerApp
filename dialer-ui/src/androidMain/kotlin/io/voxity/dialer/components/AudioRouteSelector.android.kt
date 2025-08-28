package io.voxity.dialer.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.voxity.dialer.components.CallAudioRouteSelector

@Composable
actual fun PlatformAudioRouteSelector(
    onDismiss: () -> Unit,
    onRouteSelected: () -> Unit,
    modifier: Modifier
) {
    CallAudioRouteSelector(
        onDismiss = onDismiss,
        onRouteSelected = { onRouteSelected() },
        modifier = modifier
    )
}