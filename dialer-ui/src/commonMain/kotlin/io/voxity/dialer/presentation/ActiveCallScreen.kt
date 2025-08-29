package io.voxity.dialer.presentation

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.voxity.dialer.components.CircularCallButton
import io.voxity.dialer.components.PlatformAudioRouteSelector
import io.voxity.dialer.components.SwipeableIncomingCall
import io.voxity.dialer.components.SaveContactDialog
import io.voxity.dialer.ui.state.ActiveCallScreenState
import io.voxity.dialer.ui.callbacks.ActiveCallScreenCallbacks
import io.voxity.dialer.ui.theme.CallColors
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActiveCallScreen(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks,
    onSaveContact: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F1419)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (callState.isRinging && callState.isIncoming) 1.08f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            )
        )

        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = if (callState.isRinging && callState.isIncoming) 0.7f else 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow background
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                        CircleShape
                    )
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            )

            // Main avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callState.contactName.firstOrNull()?.toString()?.uppercase()
                        ?: callState.phoneNumber.firstOrNull()?.toString() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = callState.contactName.ifEmpty { callState.phoneNumber },
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (onSaveContact != null &&
                (callState.contactName.isEmpty() || callState.contactName == callState.phoneNumber) &&
                callState.phoneNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = onSaveContact,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Save Contact",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Phone number (if different from contact name)
        if (callState.contactName.isNotEmpty() && callState.contactName != callState.phoneNumber) {
            Text(
                text = callState.phoneNumber,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call status with better styling
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = getCallStatusText(callState, callDuration),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EnhancedCallControls(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks
) {
    when {
        state.callState.isConnecting -> {
            EnhancedCircularButton(
                icon = Icons.Default.CallEnd,
                backgroundColor = CallColors.callRed,
                onClick = callbacks::onEndCall,
                contentDescription = "End call",
                size = 80.dp
            )
        }

        state.callState.isRinging && state.callState.isIncoming -> {
            SwipeableIncomingCall(
                onAnswer = callbacks::onAnswerCall,
                onReject = callbacks::onRejectCall
            )
        }

        state.callState.isActive || state.callState.isOnHold -> {
            EnhancedActiveCallControls(
                state = state,
                callbacks = callbacks
            )
        }
    }
}

@Composable
private fun EnhancedActiveCallControls(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks
) {
    var showAudioSelector by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedCircularButton(
                icon = if (state.callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                backgroundColor = if (state.callState.isMuted) CallColors.callRed else Color.White.copy(alpha = 0.15f),
                iconTint = if (state.callState.isMuted) Color.White else Color.White.copy(alpha = 0.9f),
                onClick = { callbacks.onMuteCall(!state.callState.isMuted) },
                contentDescription = if (state.callState.isMuted) "Unmute" else "Mute"
            )

            EnhancedCircularButton(
                icon = if (state.callState.isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                backgroundColor = Color.White.copy(alpha = 0.15f),
                iconTint = Color.White.copy(alpha = 0.9f),
                onClick = {
                    if (state.callState.isOnHold) {
                        callbacks.onUnholdCall()
                    } else {
                        callbacks.onHoldCall()
                    }
                },
                contentDescription = if (state.callState.isOnHold) "Unhold" else "Hold"
            )

            EnhancedCircularButton(
                icon = Icons.Default.VolumeUp,
                backgroundColor = Color.White.copy(alpha = 0.15f),
                iconTint = Color.White.copy(alpha = 0.9f),
                onClick = { showAudioSelector = true },
                contentDescription = "Audio options"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        EnhancedCircularButton(
            icon = Icons.Default.CallEnd,
            backgroundColor = CallColors.callRed,
            onClick = callbacks::onEndCall,
            contentDescription = "End call",
            size = 80.dp
        )
    }

    if (showAudioSelector) {
        PlatformAudioRouteSelector(
            onDismiss = { showAudioSelector = false },
            onRouteSelected = { showAudioSelector = false }
        )
    }
}

@Composable
private fun EnhancedCircularButton(
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    contentDescription: String,
    size: Dp = 60.dp,
    iconTint: Color = Color.White
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}

private fun getCallStatusText(callState: io.voxity.dialer.domain.models.CallState, callDuration: Long): String {
    return when {
        callState.isConnecting -> "Connecting..."
        callState.isRinging && callState.isIncoming -> "Swipe to answer or reject"
        callState.isRinging && !callState.isIncoming -> "Calling..."
        callState.isActive -> formatDuration(callDuration)
        callState.isOnHold -> "On hold"
        else -> "Connected"
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = (seconds / 60).toString().padStart(2, '0')
    val remainingSeconds = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$remainingSeconds"
}