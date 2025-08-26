package voxity.org.dialer.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voxity.org.dialer.components.CallButton
import voxity.org.dialer.models.CallState
import voxity.org.dialer.answerCall
import voxity.org.dialer.endCall
import voxity.org.dialer.rejectCall
import kotlinx.coroutines.delay
import voxity.org.dialer.getCallManagerAccess
import kotlin.time.Duration.Companion.seconds

@Composable
fun InCallScreen(
    callState: CallState,
    modifier: Modifier = Modifier
) {
    var callDuration by remember { mutableStateOf(0L) }

    // Update call duration for active calls
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
            // Top section - Call info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp)
            ) {
                // Contact avatar placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callState.contactName.firstOrNull()?.toString()?.uppercase()
                            ?: callState.phoneNumber.firstOrNull()?.toString() ?: "?",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contact name or phone number
                Text(
                    text = callState.contactName.ifEmpty { callState.phoneNumber },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                // Phone number if contact name exists
                if (callState.contactName.isNotEmpty() && callState.contactName != callState.phoneNumber) {
                    Text(
                        text = callState.phoneNumber,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Call status
                Text(
                    text = when {
                        callState.isRinging && callState.isIncoming -> "Incoming call"
                        callState.isRinging && !callState.isIncoming -> "Calling..."
                        callState.isActive -> formatDuration(callDuration)
                        callState.isOnHold -> "On hold"
                        else -> "Connected"
                    },
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Bottom section - Call controls
            CallControlButtons(callState = callState)
        }
    }
}

@Composable
private fun CallControlButtons(
    callState: CallState,
    modifier: Modifier = Modifier
) {
    when {
        callState.isRinging && callState.isIncoming -> {
            // Incoming call buttons
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject button
                CallButton(
                    icon = Icons.Default.CallEnd,
                    backgroundColor = Color.Red,
                    onClick = { rejectCall() },
                    contentDescription = "Reject call",
                    modifier = Modifier.size(72.dp)
                )

                // Answer button
                CallButton(
                    icon = Icons.Default.Call,
                    backgroundColor = Color.Green,
                    onClick = { answerCall() },
                    contentDescription = "Answer call",
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        callState.isActive || callState.isOnHold -> {
            // Active call buttons
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
                        onClick = {
                            getCallManagerAccess().muteCall(!callState.isMuted)
                        },
                        contentDescription = if (callState.isMuted) "Unmute" else "Mute"
                    )

                    CallButton(
                        icon = Icons.Default.Dialpad,
                        backgroundColor = Color.Gray,
                        onClick = { /* Show dialpad */ },
                        contentDescription = "Show dialpad"
                    )

                    CallButton(
                        icon = Icons.Default.VolumeUp,
                        backgroundColor = Color.Gray,
                        onClick = { /* Toggle speaker */ },
                        contentDescription = "Speaker"
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
                                getCallManagerAccess().unholdCall()
                            } else {
                                getCallManagerAccess().holdCall()
                            }
                        },
                        contentDescription = if (callState.isOnHold) "Unhold" else "Hold"
                    )

                    CallButton(
                        icon = Icons.Default.Add,
                        backgroundColor = Color.Gray,
                        onClick = { /* Add call */ },
                        contentDescription = "Add call"
                    )

                    CallButton(
                        icon = Icons.Default.VideoCall,
                        backgroundColor = Color.Gray,
                        onClick = { /* Toggle video */ },
                        contentDescription = "Video call"
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // End call button
                CallButton(
                    icon = Icons.Default.CallEnd,
                    backgroundColor = Color.Red,
                    onClick = { endCall() },
                    contentDescription = "End call",
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        else -> {
            // Fallback for other states
            CallButton(
                icon = Icons.Default.CallEnd,
                backgroundColor = Color.Red,
                onClick = { endCall() },
                contentDescription = "End call",
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = (seconds / 60).toString().padStart(2, '0')
    val remainingSeconds = (seconds % 60).toString().padStart(2, '0')
    return "$minutes:$remainingSeconds"
}