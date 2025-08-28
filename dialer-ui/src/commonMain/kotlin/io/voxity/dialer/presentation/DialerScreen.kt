package io.voxity.dialer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.voxity.dialer.components.DialerKeypad
import io.voxity.dialer.domain.usecases.CallUseCases

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerScreen(
    modifier: Modifier = Modifier,
    canMakeCall: Boolean = false,
    callUseCases: CallUseCases
) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar with app name
        Text(
            text = "Dialer",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Warning if not default dialer
        if (!canMakeCall) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "⚠️ Set as default dialer to make calls",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Phone number display
        Column {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = phoneNumber.ifEmpty { "Enter phone number" },
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        color = if (phoneNumber.isEmpty())
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    if (phoneNumber.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (phoneNumber.isNotEmpty()) {
                                    phoneNumber = phoneNumber.dropLast(1)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        }

        // Dial pad
        DialerKeypad(
            onNumberClick = { number ->
                phoneNumber += number
            },
            onCallClick = {
                if (phoneNumber.isNotEmpty() && canMakeCall) {
                    callUseCases.makeCall(phoneNumber)
                }
            },
            modifier = Modifier.padding(vertical = 32.dp)
        )
    }
}