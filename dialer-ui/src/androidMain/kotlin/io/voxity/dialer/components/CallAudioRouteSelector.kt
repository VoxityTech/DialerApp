package io.voxity.dialer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.voxity.dialer.audio.AudioRoute
import io.voxity.dialer.audio.AudioRouteManager

val AudioRoute.icon: ImageVector
    get() = when (this) {
        AudioRoute.SPEAKER -> Icons.Default.VolumeUp
        AudioRoute.EARPIECE -> Icons.Default.Phone
        AudioRoute.BLUETOOTH -> Icons.Default.Bluetooth
        AudioRoute.WIRED_HEADSET -> Icons.Default.Headset
    }

@Composable
fun CallAudioRouteSelector(
    onDismiss: () -> Unit,
    onRouteSelected: (AudioRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioRouteManager = remember { AudioRouteManager(context) }

    val availableRoutes by audioRouteManager.availableRoutes.collectAsState()
    val currentRoute by audioRouteManager.currentRoute.collectAsState()

    // Refresh routes when the selector is shown
    LaunchedEffect(Unit) {
        audioRouteManager.refreshRoutesForCall()
    }

    // Calculate maximum height based on screen size
    val configuration = LocalConfiguration.current
    val maxHeight = configuration.screenHeightDp.dp * 0.7f

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { it },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(300)) { it }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
                .windowInsetsPadding(WindowInsets.systemBars)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .heightIn(max = maxHeight)
                    .padding(16.dp)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                // Use LazyColumn instead of Column with verticalScroll
                LazyColumn(
                    modifier = Modifier.padding(24.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Audio Output",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = onDismiss) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(availableRoutes) { route ->
                        AudioRouteItem(
                            route = route,
                            isSelected = route == currentRoute,
                            onClick = {
                                audioRouteManager.setAudioRoute(route)
                                onRouteSelected(route)
                                onDismiss()
                            }
                        )

                        if (route != availableRoutes.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "background_color"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "border_color"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = animatedBackgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = route.icon,
                    contentDescription = route.displayName,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = route.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                exit = scaleOut(tween(200)) + fadeOut(tween(200))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}