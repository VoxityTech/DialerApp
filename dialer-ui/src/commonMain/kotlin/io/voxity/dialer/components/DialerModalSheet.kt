package io.voxity.dialer.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.voxity.dialer.components.DialerKeypad
import kotlin.math.roundToInt

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DialerModalSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit,
    initialPhoneNumber: String = "",
    modifier: Modifier = Modifier
) {
    var offsetY by remember { mutableFloatStateOf(0f) }

    var phoneNumber by remember(isVisible) {
        mutableStateOf(if (isVisible) initialPhoneNumber else "")
    }

    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isVisible) offsetY else 1000f,
        animationSpec = spring(dampingRatio = 0.8f),
        finishedListener = {
            if (!isVisible && it >= 1000f) {
                offsetY = 0f
            }
        }
    )

    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetY > 200) {
                                    onDismiss()
                                }
                                offsetY = 0f
                            }
                        ) { _, dragAmount ->
                            val newOffset = offsetY + dragAmount
                            offsetY = if (newOffset > 0) newOffset else 0f
                        }
                    },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 24.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dialer",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy–paste enabled text field
                            BasicTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = if (phoneNumber.isEmpty()) 16.sp else 20.sp,
                                    fontWeight = if (phoneNumber.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                                    fontFamily = if (phoneNumber.isEmpty()) FontFamily.Default else FontFamily.Monospace,
                                    color = if (phoneNumber.isEmpty())
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (phoneNumber.isEmpty()) {
                                        Text(
                                            "Enter phone number",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            )

                            if (phoneNumber.isNotEmpty()) {
                                Surface(
                                    onClick = { phoneNumber = phoneNumber.dropLast(1) },
                                    shape = CircleShape,
                                    color = Color.Transparent
                                ) {
                                    Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column {
                        DialerKeypad(
                            onNumberClick = { number -> phoneNumber += number },
                            onCallClick = {
                                if (phoneNumber.isNotEmpty()) onCall(phoneNumber)
                            }
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}
