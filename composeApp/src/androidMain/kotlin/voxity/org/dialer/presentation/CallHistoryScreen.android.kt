package voxity.org.dialer.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import voxity.org.dialer.data.CallLogItem
import voxity.org.dialer.data.CallLogReader
import voxity.org.dialer.domain.usecases.CallUseCases
import kotlin.text.ifEmpty

@Composable
actual fun CallHistoryScreen(
    modifier: Modifier,
    callUseCases: CallUseCases
) {
    val context = LocalContext.current
    var callHistory by remember { mutableStateOf(emptyList<CallLogItem>()) }

    LaunchedEffect(Unit) {
        callHistory = CallLogReader(context).getCallHistory()
    }

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
                        onCallClick = {
                            callUseCases.makeCall(item.phoneNumber)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallHistoryItem(
    item: CallLogItem,
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
                    "INCOMING" -> Icons.Default.CallReceived
                    "OUTGOING" -> Icons.Default.CallMade
                    "MISSED" -> Icons.Default.CallReceived
                    else -> Icons.Default.Call
                },
                contentDescription = null,
                tint = when (item.callType) {
                    "INCOMING" -> Color.Green
                    "OUTGOING" -> Color.Blue
                    "MISSED" -> Color.Red
                    else -> Color.Gray
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
                    text = "${item.date} • ${formatDuration(item.duration)}",
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