// src/commonMain/kotlin/voxity/org/dialer/presentation/CallHistoryScreen.kt
package voxity.org.dialer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voxity.org.dialer.domain.models.CallHistoryItem
import voxity.org.dialer.domain.models.CallType
import kotlin.text.ifEmpty

@Composable
fun CallHistoryScreen(
    callHistory: List<CallHistoryItem>,
    onCallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Call History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (callHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No call history found")
            }
        } else {
            LazyColumn {
                items(callHistory) { item ->
                    CallHistoryItem(
                        item = item,
                        onCallClick = { onCallClick(item.phoneNumber) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallHistoryItem(
    item: CallHistoryItem,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (item.callType) {
                    CallType.INCOMING -> Icons.Default.CallReceived
                    CallType.OUTGOING -> Icons.Default.CallMade
                    CallType.MISSED -> Icons.Default.CallReceived
                },
                contentDescription = null,
                tint = when (item.callType) {
                    CallType.INCOMING -> Color.Green
                    CallType.OUTGOING -> Color.Blue
                    CallType.MISSED -> Color.Red
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.contactName.ifEmpty { item.phoneNumber },
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                if (item.contactName.isNotEmpty()) {
                    Text(
                        text = item.phoneNumber,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "${item.timestamp} • ${formatDuration(item.duration)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onCallClick) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds == 0L) return "No duration"
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}m ${remainingSeconds}s"
}