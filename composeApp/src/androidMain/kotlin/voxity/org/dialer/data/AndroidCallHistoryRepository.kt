// src/androidMain/kotlin/voxity/org/dialer/data/AndroidCallHistoryRepository.kt
package voxity.org.dialer.data

import voxity.org.dialer.domain.repository.CallHistoryRepository
import voxity.org.dialer.domain.models.CallHistoryItem
import voxity.org.dialer.domain.models.CallType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

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
                timestamp = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                duration = androidCallLog.duration
            )
        }
    }
}