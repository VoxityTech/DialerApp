package voxity.org.dialer.screens

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
import voxity.org.dialer.models.CallHistoryItem
import voxity.org.dialer.models.CallType
import kotlinx.datetime.LocalDateTime

@Composable
fun CallHistoryScreen(
    modifier: Modifier = Modifier
) {
    // Sample call history data
    val callHistory = remember {
        listOf(
            CallHistoryItem(
                phoneNumber = "+1234567890",
                contactName = "John Doe",
                callType = CallType.OUTGOING,
                timestamp = LocalDateTime(2023, 12, 15, 14, 30),
                duration = 125
            ),
            CallHistoryItem(
                phoneNumber = "+0987654321",
                contactName = "Jane Smith",
                callType = CallType.INCOMING,
                timestamp = LocalDateTime(2023, 12, 15, 12, 15),
                duration = 78
            ),
            CallHistoryItem(
                phoneNumber = "+1122334455",
                contactName = "",
                callType = CallType.MISSED,
                timestamp = LocalDateTime(2023, 12, 15, 9, 45),
                duration = 0
            )
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Call History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(callHistory) { item ->
                CallHistoryItem(
                    item = item,
                    onCallClick = { /* Make call to this number */ }
                )
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call type icon
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

            // Contact info
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
                    text = "${item.timestamp.time} • ${formatDuration(item.duration)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Call button
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