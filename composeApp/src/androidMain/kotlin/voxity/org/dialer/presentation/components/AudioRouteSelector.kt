package voxity.org.dialer.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voxity.org.dialer.audio.AudioRouteManager

enum class AudioRoute(val displayName: String, val icon: ImageVector) {
    SPEAKER("Speaker", Icons.Default.VolumeUp),
    EARPIECE("Earpiece", Icons.Default.Phone),
    BLUETOOTH("Bluetooth", Icons.Default.Bluetooth),
    WIRED_HEADSET("Wired Headset", Icons.Default.Headset)
}

@Composable
fun AudioRouteSelector(
    onDismiss: () -> Unit,
    onRouteSelected: (AudioRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioRouteManager = remember { AudioRouteManager(context) }
    var availableRoutes by remember { mutableStateOf(audioRouteManager.getAvailableRoutes()) }
    var currentRoute by remember { mutableStateOf(audioRouteManager.getCurrentRoute()) }

    LaunchedEffect(Unit) {
        availableRoutes = audioRouteManager.getAvailableRoutes()
        currentRoute = audioRouteManager.getCurrentRoute()
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(enabled = false) { }, // Prevent dismissal when clicking card
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Audio Output",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    availableRoutes.forEach { route ->
                        AudioRouteItem(
                            route = route,
                            isSelected = route == currentRoute,
                            onClick = {
                                audioRouteManager.setAudioRoute(route)
                                onRouteSelected(route)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioRouteItem(
    route: AudioRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = route.icon,
            contentDescription = route.displayName,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = route.displayName,
            fontSize = 16.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}