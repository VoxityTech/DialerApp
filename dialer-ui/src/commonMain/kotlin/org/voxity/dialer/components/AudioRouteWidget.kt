package org.voxity.dialer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

interface AudioRouteInfo {
    val displayName: String
    val icon: ImageVector
}

@Composable
fun AudioRouteWidget(
    route: AudioRouteInfo,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = when {
            !isAvailable -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label = "audio_route_background"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = when {
            !isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            isSelected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "audio_route_icon"
    )

    Surface(
        onClick = if (isAvailable) onClick else { {} },
        shape = CircleShape,
        color = animatedBackgroundColor,
        shadowElevation = if (isAvailable) 4.dp else 0.dp,
        modifier = modifier.size(56.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = route.icon,
                contentDescription = if (isAvailable) route.displayName else "${route.displayName} (unavailable)",
                tint = animatedIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}