package org.voxity.dialer.utils

import org.voxity.dialer.domain.models.CallHistoryItem
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object CallLogFormatter {
    @OptIn(ExperimentalTime::class)
    fun formatCallDate(timestamp: LocalDateTime): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val callDate = timestamp.date

        return when {
            callDate == today -> "Today"
            callDate == yesterday -> "Yesterday"
            else -> "${callDate.dayOfMonth} ${callDate.month.name.lowercase().capitalize()} ${callDate.year}"
        }
    }

    fun groupCallsByDate(items: List<CallHistoryItem>): Map<String, List<CallHistoryItem>> {
        return items.groupBy { formatCallDate(it.timestamp) }
    }
}