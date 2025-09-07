package org.voxity.dialer.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.voxity.dialer.ui.theme.CallColors
import kotlin.math.roundToInt

@Composable
fun SwipeableIncomingCall(
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var shouldAnimateBack by remember { mutableStateOf(false) }

    // Animate back to center after incomplete swipe
    LaunchedEffect(shouldAnimateBack) {
        if (shouldAnimateBack) {
            animate(offsetX, 0f) { value, _ -> offsetX = value }
            shouldAnimateBack = false
        }
    }

    // Color changes dynamically based on swipe direction
    val backgroundColor by remember {
        derivedStateOf {
            when {
                offsetX > 0 -> CallColors.callGreen.copy(alpha = minOf(offsetX / swipeThreshold, 1f))
                offsetX < 0 -> CallColors.callRed.copy(alpha = minOf(-offsetX / swipeThreshold, 1f))
                else -> CallColors.dialerBackground.copy(alpha = 0.5f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(90.dp)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold -> onAnswer()
                                offsetX < -swipeThreshold -> onReject()
                                else -> shouldAnimateBack = true
                            }
                        }
                    ) { _, dragAmount ->
                        offsetX += dragAmount.x
                    }
                }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = if (offsetX >= 0) Icons.Default.Call else Icons.Default.CallEnd,
                    contentDescription = "Swipe to Answer or Reject",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
