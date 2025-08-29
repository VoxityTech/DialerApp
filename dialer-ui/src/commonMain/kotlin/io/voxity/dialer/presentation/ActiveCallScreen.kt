package io.voxity.dialer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
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
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedCallInfo(
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

            AnimatedCallControls(
                state = state,
                callbacks = callbacks
            )
        }
    }

    // Save contact dialog
    SaveContactDialog(
        phoneNumber = state.callState.phoneNumber,
        isVisible = showSaveContactDialog,
        onSave = { contactName ->
            onSaveContact?.invoke(contactName, state.callState.phoneNumber)
        },
        onDismiss = {
            showSaveContactDialog = false
            // Reset any internal dialog state if needed
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCallInfo(
    callState: io.voxity.dialer.domain.models.CallState,
    callDuration: Long,
    onSaveContact: (() -> Unit)? = null
) {
    AnimatedContent(
        targetState = callState,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
        }
    ) { state ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 64.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (state.isRinging && state.isIncoming) 1.1f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.contactName.firstOrNull()?.toString()?.uppercase()
                        ?: state.phoneNumber.firstOrNull()?.toString() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.contactName.ifEmpty { state.phoneNumber },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                if (onSaveContact != null &&
                    (state.contactName.isEmpty() || state.contactName == state.phoneNumber) &&
                    state.phoneNumber.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onSaveContact,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Save Contact",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (state.contactName.isNotEmpty() && state.contactName != state.phoneNumber) {
                Text(
                    text = state.phoneNumber,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = getCallStatusText(state, callDuration),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AnimatedCallControls(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks
) {
    when {
        state.callState.isConnecting -> {
            CircularCallButton(
                icon = Icons.Default.CallEnd,
                backgroundColor = CallColors.callRed,
                onClick = callbacks::onEndCall,
                contentDescription = "End call",
                modifier = Modifier.size(80.dp)
            )
        }

        state.callState.isRinging && state.callState.isIncoming -> {
            SwipeableIncomingCall(
                onAnswer = callbacks::onAnswerCall,
                onReject = callbacks::onRejectCall
            )
        }

        state.callState.isActive || state.callState.isOnHold -> {
            ActiveCallControls(
                state = state,
                callbacks = callbacks
            )
        }
    }
}

@Composable
private fun ActiveCallControls(
    state: ActiveCallScreenState,
    callbacks: ActiveCallScreenCallbacks
) {
    var showAudioSelector by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularCallButton(
                icon = if (state.callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                backgroundColor = if (state.callState.isMuted) CallColors.callRed else MaterialTheme.colorScheme.surfaceVariant,
                onClick = { callbacks.onMuteCall(!state.callState.isMuted) },
                contentDescription = if (state.callState.isMuted) "Unmute" else "Mute"
            )

            CircularCallButton(
                icon = if (state.callState.isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    if (state.callState.isOnHold) {
                        callbacks.onUnholdCall()
                    } else {
                        callbacks.onHoldCall()
                    }
                },
                contentDescription = if (state.callState.isOnHold) "Unhold" else "Hold"
            )

            CircularCallButton(
                icon = Icons.Default.VolumeUp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { showAudioSelector = true },
                contentDescription = "Audio options"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        CircularCallButton(
            icon = Icons.Default.CallEnd,
            backgroundColor = CallColors.callRed,
            onClick = callbacks::onEndCall,
            contentDescription = "End call",
            modifier = Modifier.size(80.dp)
        )
    }

    // Add audio selector dialog
    if (showAudioSelector) {
        PlatformAudioRouteSelector(
            onDismiss = { showAudioSelector = false },
            onRouteSelected = { showAudioSelector = false }
        )
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