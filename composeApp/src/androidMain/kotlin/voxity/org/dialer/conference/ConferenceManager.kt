package voxity.org.dialer.conference

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConferenceState(
    val isConferenceActive: Boolean = false,
    val participants: List<Call> = emptyList(),
    val canMerge: Boolean = false,
    val canSeparate: Boolean = false
)

class ConferenceManager {

    private val _conferenceState = MutableStateFlow(ConferenceState())
    val conferenceState: StateFlow<ConferenceState> = _conferenceState.asStateFlow()

    fun updateConferenceState(calls: List<Call>) {
        val conferenceCall = calls.find { it.details.hasProperty(Call.Details.PROPERTY_CONFERENCE) }
        val participants = if (conferenceCall != null) {
            conferenceCall.children + listOf(conferenceCall)
        } else {
            emptyList()
        }

        _conferenceState.value = ConferenceState(
            isConferenceActive = conferenceCall != null,
            participants = participants,
            canMerge = canMergeCalls(calls),
            canSeparate = participants.isNotEmpty()
        )
    }

    fun mergeCall(primaryCall: Call, secondaryCall: Call): Boolean {
        return try {
            primaryCall.conference(secondaryCall)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun separateCall(call: Call): Boolean {
        return try {
            call.splitFromConference()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun disconnectFromConference(call: Call): Boolean {
        return try {
            call.disconnect()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun canMergeCalls(calls: List<Call>): Boolean {
        val activeCalls = calls.filter {
            it.state == Call.STATE_ACTIVE || it.state == Call.STATE_HOLDING
        }
        return activeCalls.size >= 2
    }
}