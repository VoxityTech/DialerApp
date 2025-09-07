package org.voxity.dialer.data

import org.voxity.dialer.domain.repository.CallHistoryRepository
import org.voxity.dialer.domain.models.CallHistoryItem
import org.voxity.dialer.domain.models.CallType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AndroidCallHistoryRepository(
    private val callLogReader: CallLogReader
) : CallHistoryRepository {

    @OptIn(ExperimentalTime::class)
    override suspend fun getCallHistory(): List<CallHistoryItem> {
        return callLogReader.getCallHistory().map { androidCallLog ->
            CallHistoryItem(
                phoneNumber = androidCallLog.phoneNumber,
                contactName = androidCallLog.contactName,
                callType = when (androidCallLog.callType) {
                    "INCOMING" -> CallType.INCOMING
                    "OUTGOING" -> CallType.OUTGOING
                    "MISSED" -> CallType.MISSED
                    else -> CallType.MISSED
                },
                timestamp = parseCallLogDate(androidCallLog.date),
                duration = androidCallLog.duration
            )
        }.sortedByDescending { it.timestamp }
    }

    @OptIn(ExperimentalTime::class)
    private fun parseCallLogDate(dateString: String): LocalDateTime {
        return try {
            // Parse as timestamp if it's a long
            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                Instant.fromEpochMilliseconds(timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            } else {
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        } catch (e: Exception) {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}