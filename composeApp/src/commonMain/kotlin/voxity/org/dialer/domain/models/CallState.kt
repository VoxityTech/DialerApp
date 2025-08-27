package voxity.org.dialer.domain.models

import kotlinx.datetime.LocalDateTime

data class CallState(
    val isActive: Boolean = false,
    val phoneNumber: String = "",
    val contactName: String = "",
    val callDuration: Long = 0L,
    val isIncoming: Boolean = false,
    val isRinging: Boolean = false,
    val isOnHold: Boolean = false,
    val isMuted: Boolean = false,
    val isConnecting: Boolean = false,
    val callStartTime: LocalDateTime? = null
)

data class CallHistoryItem(
    val phoneNumber: String,
    val contactName: String,
    val callType: CallType,
    val timestamp: LocalDateTime,
    val duration: Long
)

enum class CallType {
    INCOMING, OUTGOING, MISSED
}