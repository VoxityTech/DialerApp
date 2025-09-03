package org.voxity.dialer.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformAudioRouteWidgets(
    onRouteSelected: () -> Unit,
    modifier: Modifier = Modifier
)