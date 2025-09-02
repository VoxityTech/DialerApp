package io.voxity.dialer.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.voxity.dialer.components.PlatformAudioRouteWidgets
import io.voxity.dialer.components.SwipeableIncomingCall
import io.voxity.dialer.components.SaveContactDialog
import io.voxity.dialer.ui.state.ActiveCallScreenState
import io.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActiveCallScreen(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks,
    onSaveContact: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var callDuration by remember { mutableStateOf(0L) }
    var showSaveContactDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.callState.isActive) {
        if (state.callState.isActive) {
            while (state.callState.isActive) {
                delay(1.seconds)
                callDuration++
            }
        } else {
            callDuration = 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EnhancedCallInfo(
            callState = state.callState,
            callDuration = callDuration,
            onSaveContact = onSaveContact?.let { saveCallback ->
                {
                    if (state.callState.contactName.isEmpty() ||
                        state.callState.contactName == state.callState.phoneNumber) {
                        showSaveContactDialog = true
                    }
                }
            }
        )

        EnhancedCallControls(
            state = state,
            callbacks = callbacks
        )
    }

    if (showSaveContactDialog) {
        SaveContactDialog(
            phoneNumber = state.callState.phoneNumber,
            isVisible = showSaveContactDialog,
            onSave = { contactName ->
                onSaveContact?.invoke(contactName, state.callState.phoneNumber)
            },
            onDismiss = {
                showSaveContactDialog = false
            }
        )
    }
}

@Composable
private fun EnhancedCallInfo(
    callState: io.voxity.dialer.domain.models.CallState,
    callDuration: Long,
    onSaveContact: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 60.dp)
    ) {
        // Contact info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                CallAvatar(
                    callState = callState,
                    size = 100.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Contact name and actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = callState.contactName.ifEmpty { callState.phoneNumber },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        // Phone number (if different from contact name)
                        if (callState.contactName.isNotEmpty() &&
                            callState.contactName != callState.phoneNumber) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = callState.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Save contact button
                    if (onSaveContact != null &&
                        (callState.contactName.isEmpty() ||
                                callState.contactName == callState.phoneNumber) &&
                        callState.phoneNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = onSaveContact,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Save Contact",
                                tint = colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Call status
                CallStatusChip(
                    callState = callState,
                    callDuration = callDuration
                )
            }
        }
    }
}

@Composable
private fun CallAvatar(
    callState: io.voxity.dialer.domain.models.CallState,
    size: Dp
) {
    val colorScheme = MaterialTheme.colorScheme

    // Animate for ringing calls
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (callState.isRinging && callState.isIncoming) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Pulse ring for incoming calls
        if (callState.isRinging && callState.isIncoming) {
            Surface(
                modifier = Modifier
                    .size(size + 20.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = (2f - scale) * 0.3f
                    ),
                shape = CircleShape,
                color = colorScheme.primary.copy(alpha = 0.3f)
            ) {}
        }

        // Main avatar
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val displayChar = callState.contactName.firstOrNull()?.toString()?.uppercase()
                    ?: callState.phoneNumber.firstOrNull()?.toString()
                    ?: "?"

                Text(
                    text = displayChar,
                    fontSize = (size.value * 0.4f).sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CallStatusChip(
    callState: io.voxity.dialer.domain.models.CallState,
    callDuration: Long
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusText = getCallStatusText(callState, callDuration)

    val chipColor = when {
        callState.isConnecting -> colorScheme.secondaryContainer
        callState.isRinging -> if (callState.isIncoming)
            colorScheme.primaryContainer else colorScheme.secondaryContainer
        callState.isActive -> colorScheme.primaryContainer
        callState.isOnHold -> colorScheme.tertiaryContainer
        else -> colorScheme.surfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipColor
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = when {
                callState.isConnecting -> colorScheme.onSecondaryContainer
                callState.isRinging -> if (callState.isIncoming)
                    colorScheme.onPrimaryContainer else colorScheme.onSecondaryContainer
                callState.isActive -> colorScheme.onPrimaryContainer
                callState.isOnHold -> colorScheme.onTertiaryContainer
                else -> colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun EnhancedCallControls(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks
) {
    when {
        state.callState.isConnecting -> {
            ModernCallButton(
                icon = Icons.Default.CallEnd,
                backgroundColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                onClick = callbacks::onEndCall,
                contentDescription = "End call",
                size = 72.dp
            )
        }

        state.callState.isRinging && state.callState.isIncoming -> {
            SwipeableIncomingCall(
                onAnswer = callbacks::onAnswerCall,
                onReject = callbacks::onRejectCall
            )
        }

        state.callState.isActive || state.callState.isOnHold -> {
            ModernActiveCallControls(
                state = state,
                callbacks = callbacks
            )
        }
    }
}

@Composable
private fun ModernActiveCallControls(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Audio route widgets row
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Audio Output",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                PlatformAudioRouteWidgets(
                    onRouteSelected = {
                        // Route is already set by the widget, just provide feedback if needed
                        // No parameters needed since the widget handles everything internally
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call control buttons row
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ModernCallButton(
                    icon = if (state.callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    backgroundColor = if (state.callState.isMuted)
                        colorScheme.error else colorScheme.surfaceVariant,
                    contentColor = if (state.callState.isMuted)
                        colorScheme.onError else colorScheme.onSurfaceVariant,
                    onClick = { callbacks.onMuteCall(!state.callState.isMuted) },
                    contentDescription = if (state.callState.isMuted) "Unmute" else "Mute"
                )

                ModernCallButton(
                    icon = if (state.callState.isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                    backgroundColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                    onClick = {
                        if (state.callState.isOnHold) {
                            callbacks.onUnholdCall()
                        } else {
                            callbacks.onHoldCall()
                        }
                    },
                    contentDescription = if (state.callState.isOnHold) "Unhold" else "Hold"
                )

                ModernCallButton(
                    icon = Icons.Default.GroupAdd,
                    backgroundColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                    onClick = {
                        callbacks.onAddCall()
                    },
                    contentDescription = "Add call to conference"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // End call button
        ModernCallButton(
            icon = Icons.Default.CallEnd,
            backgroundColor = colorScheme.error,
            contentColor = colorScheme.onError,
            onClick = callbacks::onEndCall,
            contentDescription = "End call",
            size = 72.dp
        )
    }
}

@Composable
private fun ModernCallButton(
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    contentDescription: String,
    size: Dp = 56.dp
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        containerColor = backgroundColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.4f)
        )
    }
}

private fun getCallStatusText(
    callState: io.voxity.dialer.domain.models.CallState,
    callDuration: Long
): String {
    return when {
        callState.isConnecting -> "Connecting..."
        callState.isRinging && callState.isIncoming -> "Incoming call"
        callState.isRinging && !callState.isIncoming -> "Calling..."
        callState.isActive -> formatDuration(callDuration)
        callState.isOnHold -> "On hold"
        else -> "Connected"
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}