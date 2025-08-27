/*
package voxity.org.dialer.callwaiting

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CallWaitingState(
    val hasWaitingCall: Boolean = false,
    val activeCall: Call? = null,
    val waitingCall: Call? = null,
    val canSwap: Boolean = false,
    val canMerge: Boolean = false
)

class CallWaitingManager {

    private val _callWaitingState = MutableStateFlow(CallWaitingState())
    val callWaitingState: StateFlow<CallWaitingState> = _callWaitingState.asStateFlow()

    fun updateCalls(calls: List<Call>) {
        val activeCalls = calls.filter {
            it.state == Call.STATE_ACTIVE || it.state == Call.STATE_HOLDING
        }
        val ringingCalls = calls.filter { it.state == Call.STATE_RINGING }

        when {
            activeCalls.size == 1 && ringingCalls.size == 1 -> {
                // Call waiting scenario
                _callWaitingState.value = CallWaitingState(
                    hasWaitingCall = true,
                    activeCall = activeCalls.first(),
                    waitingCall = ringingCalls.first(),
                    canSwap = true,
                    canMerge = false
                )
            }
            activeCalls.size == 2 -> {
                // Two active calls
                val activeCall = activeCalls.find { it.state == Call.STATE_ACTIVE }
                val heldCall = activeCalls.find { it.state == Call.STATE_HOLDING }

                _callWaitingState.value = CallWaitingState(
                    hasWaitingCall = true,
                    activeCall = activeCall ?: activeCalls.first(),
                    waitingCall = heldCall ?: activeCalls.last(),
                    canSwap = true,
                    canMerge = true
                )
            }
            else -> {
                _callWaitingState.value = CallWaitingState()
            }
        }
    }

    fun answerWaitingCall(): Boolean {
        val state = _callWaitingState.value
        return if (state.hasWaitingCall && state.waitingCall != null) {
            // Put current call on hold and answer waiting call
            state.activeCall?.hold()
            state.waitingCall.answer(0)
            true
        } else {
            false
        }
    }

    fun rejectWaitingCall(): Boolean {
        val state = _callWaitingState.value
        return if (state.hasWaitingCall && state.waitingCall != null) {
            state.waitingCall.reject(false, "User rejected")
            true
        } else {
            false
        }
    }

    fun swapCalls(): Boolean {
        val state = _callWaitingState.value
        return if (state.canSwap && state.activeCall != null && state.waitingCall != null) {
            when (state.waitingCall.state) {
                Call.STATE_HOLDING -> {
                    state.activeCall.hold()
                    state.waitingCall.unhold()
                }
                Call.STATE_RINGING -> {
                    state.activeCall.hold()
                    state.waitingCall.answer(0)
                }
            }
            true
        } else {
            false
        }
    }
}*/
