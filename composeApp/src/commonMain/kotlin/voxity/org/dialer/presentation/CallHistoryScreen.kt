package voxity.org.dialer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import voxity.org.dialer.domain.models.CallHistoryItem
import voxity.org.dialer.domain.models.CallType
import voxity.org.dialer.ui.theme.CallColors

@Composable
fun CallHistoryScreen(
    callHistory: List<CallHistoryItem>,
    onCallClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Text(
            text = "Recent",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        if (callHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No call history",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(callHistory) { item ->
                    CallHistoryItemCard(
                        item = item,
                        onCallClick = { onCallClick(item.phoneNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallHistoryItemCard(
    item: CallHistoryItem,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call type icon with colored background
            Surface(
                shape = CircleShape,
                color = when (item.callType) {
                    CallType.INCOMING -> CallColors.callGreen.copy(alpha = 0.1f)
                    CallType.OUTGOING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    CallType.MISSED -> CallColors.callRed.copy(alpha = 0.1f)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = when (item.callType) {
                        CallType.INCOMING -> Icons.Default.CallReceived
                        CallType.OUTGOING -> Icons.Default.CallMade
                        CallType.MISSED -> Icons.Default.CallMissed
                    },
                    contentDescription = null,
                    tint = when (item.callType) {
                        CallType.INCOMING -> CallColors.callGreen
                        CallType.OUTGOING -> MaterialTheme.colorScheme.primary
                        CallType.MISSED -> CallColors.callRed
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contact info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.contactName.ifEmpty { item.phoneNumber },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.contactName.isNotEmpty()) {
                    Text(
                        text = item.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.timestamp.toString().take(16),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.duration > 0) {
                        Text(
                            text = " • ${formatDuration(item.duration)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Call button with proper contrast like ContactsScreen
            Surface(
                onClick = onCallClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Same as ContactsScreen
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Call back",
                    tint = MaterialTheme.colorScheme.primary, // Same as ContactsScreen
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds == 0L) return "0s"
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return when {
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${seconds}s"
    }
}