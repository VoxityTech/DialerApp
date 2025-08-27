
package voxity.org.dialer.presentation

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
import voxity.org.dialer.domain.models.CallState
import voxity.org.dialer.domain.usecases.CallUseCases
import voxity.org.dialer.presentation.components.CallButton
import kotlinx.coroutines.delay
import voxity.org.dialer.presentation.components.AudioRouteSelector
import kotlin.time.Duration.Companion.seconds

@Composable
actual fun InCallScreen(
    callState: CallState,
    callUseCases: CallUseCases,
    modifier: Modifier
) {
    var callDuration by remember { mutableStateOf(0L) }
    var showAudioRoutes by remember { mutableStateOf(false) }

    // Real-time call duration timer
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

    // Animated transition for call screen
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        )
    ) {
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
                // Top section - Call info with animations
                AnimatedCallInfo(
                    callState = callState,
                    callDuration = callDuration
                )

                // Bottom section - Call controls with smooth transitions
                AnimatedCallControls(
                    callState = callState,
                    callUseCases = callUseCases,
                    onShowAudioRoutes = { showAudioRoutes = true }
                )
            }

            // Audio route selector overlay
            if (showAudioRoutes) {
                AudioRouteSelector(
                    onDismiss = { showAudioRoutes = false },
                    onRouteSelected = { route ->
                        // Handle audio route selection
                        showAudioRoutes = false
                    }
                )
            }
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
            // Contact avatar with pulse animation for incoming calls
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
                    .background(MaterialTheme.colorScheme.primary)
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.contactName.firstOrNull()?.toString()?.uppercase()
                        ?: state.phoneNumber.firstOrNull()?.toString() ?: "?",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact name with slide animation
            AnimatedContent(
                targetState = state.contactName.ifEmpty { state.phoneNumber },
                transitionSpec = {
                    slideInVertically { -it } + fadeIn() with
                            slideOutVertically { it } + fadeOut()
                }
            ) { name ->
                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            // Phone number if contact name exists
            if (state.contactName.isNotEmpty() && state.contactName != state.phoneNumber) {
                Text(
                    text = state.phoneNumber,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Call status with smooth transitions
            AnimatedContent(
                targetState = getCallStatusText(state, callDuration),
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200))
                }
            ) { statusText ->
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCallControls(
    callState: CallState,
    callUseCases: CallUseCases,
    onShowAudioRoutes: () -> Unit
) {
    AnimatedContent(
        targetState = callState.isRinging to callState.isIncoming,
        transitionSpec = {
            slideInVertically { it } + fadeIn() with
                    slideOutVertically { -it } + fadeOut()
        }
    ) { (isRinging, isIncoming) ->
        when {
            callState.isConnecting -> {
                // Connecting state - only show end call
                CallButton(
                    icon = Icons.Default.CallEnd,
                    backgroundColor = Color.Red,
                    onClick = { callUseCases.endCall() },
                    contentDescription = "End call",
                    modifier = Modifier.size(80.dp)
                )
            }

            isRinging && isIncoming -> {
                // Incoming call controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallButton(
                        icon = Icons.Default.CallEnd,
                        backgroundColor = Color.Red,
                        onClick = { callUseCases.rejectCall() },
                        contentDescription = "Reject call",
                        modifier = Modifier.size(72.dp)
                    )

                    CallButton(
                        icon = Icons.Default.Call,
                        backgroundColor = Color.Green,
                        onClick = { callUseCases.answerCall() },
                        contentDescription = "Answer call",
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            callState.isActive || callState.isOnHold -> {
                // Active call controls with enhanced audio routing
                ActiveCallControls(
                    callState = callState,
                    callUseCases = callUseCases,
                    onShowAudioRoutes = onShowAudioRoutes
                )
            }
        }
    }
}

@Composable
private fun ActiveCallControls(
    callState: CallState,
    callUseCases: CallUseCases,
    onShowAudioRoutes: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // First row of controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallButton(
                icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                backgroundColor = if (callState.isMuted) Color.Red else Color.Gray,
                onClick = { callUseCases.muteCall(!callState.isMuted) },
                contentDescription = if (callState.isMuted) "Unmute" else "Mute"
            )

            CallButton(
                icon = Icons.Default.Dialpad,
                backgroundColor = Color.Gray,
                onClick = { /*Show Dial pad */},
                contentDescription = "Show dialpad"
            )

            CallButton(
                icon = Icons.Default.VolumeUp,
                backgroundColor = Color.Gray,
                onClick = onShowAudioRoutes,
                contentDescription = "Audio options"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Second row of controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallButton(
                icon = if (callState.isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                backgroundColor = Color.Gray,
                onClick = {
                    if (callState.isOnHold) {
                        callUseCases.unholdCall()
                    } else {
                        callUseCases.holdCall()
                    }
                },
                contentDescription = if (callState.isOnHold) "Unhold" else "Hold"
            )

            CallButton(
                icon = Icons.Default.Add,
                backgroundColor = Color.Gray,
                onClick = {
// Add call
 },
                contentDescription = "Add call"
            )

            CallButton(
                icon = Icons.Default.VideoCall,
                backgroundColor = Color.Gray,
                onClick = {
// Toggle video
 },
                contentDescription = "Video call"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // End call button
        CallButton(
            icon = Icons.Default.CallEnd,
            backgroundColor = Color.Red,
            onClick = { callUseCases.endCall() },
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
