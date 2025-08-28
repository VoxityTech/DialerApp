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
import io.voxity.dialer.domain.models.CallState
import io.voxity.dialer.ui.theme.CallColors
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds



@Composable
fun ActiveCallView(
    callState: CallState,
    onAnswerCall: () -> Unit,
    onRejectCall: () -> Unit,
    onEndCall: () -> Unit,
    onHoldCall: () -> Unit,
    onUnholdCall: () -> Unit,
    onMuteCall: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var callDuration by remember { mutableStateOf(0L) }

    LaunchedEffect(callState.isActive) {
        if (callState.isActive) {
            while (callState.isActive) {
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
                callState = callState,
                callDuration = callDuration
            )

            AnimatedCallControls(
                callState = callState,
                onAnswerCall = onAnswerCall,
                onRejectCall = onRejectCall,
                onEndCall = onEndCall,
                onHoldCall = onHoldCall,
                onUnholdCall = onUnholdCall,
                onMuteCall = onMuteCall
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCallInfo(
    callState: CallState,
    callDuration: Long
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
                    .background(MaterialTheme.colorScheme.primary), // Uses black in light mode, white in dark mode
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.contactName.firstOrNull()?.toString()?.uppercase()
                        ?: state.phoneNumber.firstOrNull()?.toString() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary, // Uses white in light mode, black in dark mode
                    modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = state.contactName.ifEmpty { state.phoneNumber },
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCallControls(
    callState: CallState,
    onAnswerCall: () -> Unit,
    onRejectCall: () -> Unit,
    onEndCall: () -> Unit,
    onHoldCall: () -> Unit,
    onUnholdCall: () -> Unit,
    onMuteCall: (Boolean) -> Unit
) {
    when {
        callState.isConnecting -> {
            CircularCallButton(
                icon = Icons.Default.CallEnd,
                backgroundColor = Color.Red,
                onClick = onEndCall,
                contentDescription = "End call",
                modifier = Modifier.size(80.dp)
            )
        }


        callState.isRinging && callState.isIncoming -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularCallButton(
                    icon = Icons.Default.CallEnd,
                    backgroundColor = CallColors.callRed,
                    onClick = onRejectCall,
                    contentDescription = "Reject call",
                    modifier = Modifier.size(72.dp)
                )

                CircularCallButton(
                    icon = Icons.Default.Call,
                    backgroundColor = CallColors.callGreen,
                    onClick = onAnswerCall,
                    contentDescription = "Answer call",
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        callState.isActive || callState.isOnHold -> {
            ActiveCallControls(
                callState = callState,
                onEndCall = onEndCall,
                onHoldCall = onHoldCall,
                onUnholdCall = onUnholdCall,
                onMuteCall = onMuteCall
            )
        }
    }
}

@Composable
private fun ActiveCallControls(
    callState: CallState,
    onEndCall: () -> Unit,
    onHoldCall: () -> Unit,
    onUnholdCall: () -> Unit,
    onMuteCall: (Boolean) -> Unit
) {
    var showAudioSelector by remember { mutableStateOf(false) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularCallButton(
                icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                backgroundColor = if (callState.isMuted) CallColors.callRed else MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onMuteCall(!callState.isMuted) },
                contentDescription = if (callState.isMuted) "Unmute" else "Mute"
            )

            CircularCallButton(
                icon = if (callState.isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = {
                    if (callState.isOnHold) {
                        onUnholdCall()
                    } else {
                        onHoldCall()
                    }
                },
                contentDescription = if (callState.isOnHold) "Unhold" else "Hold"
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
            onClick = onEndCall,
            contentDescription = "End call",
            modifier = Modifier.size(80.dp)
        )
    }
}


private fun getCallStatusText(callState: CallState, callDuration: Long): String {
    return when {
        callState.isConnecting -> "Connecting..."
        callState.isRinging && callState.isIncoming -> "Incoming call"
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