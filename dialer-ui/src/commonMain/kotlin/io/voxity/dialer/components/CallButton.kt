package io.voxity.dialer.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.voxity.dialer.ui.theme.CallColors

enum class CallButtonStyle {
    PRIMARY,    // Green call button
    DANGER,     // Red end/reject button
    SECONDARY   // Gray utility button
}

@Composable
fun CallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CallButtonStyle = CallButtonStyle.PRIMARY,
    icon: ImageVector = Icons.Default.Call,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: CallButtonSize = CallButtonSize.MEDIUM
) {
    val backgroundColor = when (style) {
        CallButtonStyle.PRIMARY -> CallColors.callGreen
        CallButtonStyle.DANGER -> CallColors.callRed
        CallButtonStyle.SECONDARY -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = when (style) {
        CallButtonStyle.PRIMARY, CallButtonStyle.DANGER -> Color.White
        CallButtonStyle.SECONDARY -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val buttonSize = when (size) {
        CallButtonSize.SMALL -> 48.dp
        CallButtonSize.MEDIUM -> 64.dp
        CallButtonSize.LARGE -> 80.dp
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = if (enabled) 4.dp else 0.dp,
        enabled = enabled,
        modifier = modifier.size(buttonSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(buttonSize * 0.375f) // Icon is 3/8 of button size
        )
    }
}

enum class CallButtonSize {
    SMALL, MEDIUM, LARGE
}