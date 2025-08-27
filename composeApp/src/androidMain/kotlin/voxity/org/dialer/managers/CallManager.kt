// src/androidMain/kotlin/voxity/org/dialer/managers/CallManager.kt
package voxity.org.dialer.managers

import android.content.Context
import android.telecom.Call
import android.telecom.Connection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import voxity.org.dialer.domain.models.CallState
import voxity.org.dialer.domain.repository.CallRepository
import voxity.org.dialer.notifications.CallNotificationManager
import voxity.org.dialer.audio.CallRingtoneManager
import voxity.org.dialer.blocking.ContactBlockManager
import kotlin.time.ExperimentalTime

class CallManager private constructor(private val context: Context) : CallRepository {

    private val TAG = "CallManager"

    private val _activeCalls = MutableStateFlow<List<Call>>(emptyList())
    val activeCalls: StateFlow<List<Call>> = _activeCalls.asStateFlow()

    private val _activeConnections = MutableStateFlow<List<Connection>>(emptyList())
    val activeConnections: StateFlow<List<Connection>> = _activeConnections.asStateFlow()

    private val _currentCallState = MutableStateFlow(CallState())
    override val currentCallState: StateFlow<CallState> = _currentCallState.asStateFlow()

    // New components
    private val notificationManager = CallNotificationManager(context)
    private val ringtoneManager = CallRingtoneManager(context)
    private val blockManager = ContactBlockManager.getInstance(context)

    companion object {
        @Volatile
        private var INSTANCE: CallManager? = null

        fun getInstance(context: Context? = null): CallManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CallManager(
                    context?.applicationContext
                        ?: throw IllegalArgumentException("Context required for first initialization")
                ).also { INSTANCE = it }
            }
        }
    }

    override fun makeCall(phoneNumber: String) {
        // Check if number is blocked before making call
        if (blockManager.isNumberBlocked(phoneNumber)) {
            // Optionally notify user that the number is blocked
            return
        }

        _currentCallState.value = CallState(
            isConnecting = true,
            phoneNumber = phoneNumber,
            contactName = phoneNumber,
            isIncoming = false
        )
        voxity.org.dialer.makeCall(phoneNumber)
    }

    fun addCall(call: Call) {
        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.add(call)
        _activeCalls.value = currentCalls

        // Handle incoming call
        if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
            handleIncomingCall(call)
        }

        updateCurrentCallState()
    }

    fun removeCall(call: Call) {
        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.remove(call)
        _activeCalls.value = currentCalls

        // Stop ringtone and cancel notification when call ends
        ringtoneManager.stopRinging()
        notificationManager.cancelCallNotification()

        updateCurrentCallState()
    }

    // Add missing methods for Connection handling
    fun addConnection(connection: Connection) {
        val currentConnections = _activeConnections.value.toMutableList()
        currentConnections.add(connection)
        _activeConnections.value = currentConnections
        updateConnectionState()
    }

    fun updateConnectionState() {
        updateCurrentCallState()
    }

    fun updateCallDetails() {
        updateCurrentCallState()
    }

    fun updateCallState() {
        updateCurrentCallState()
    }

    private fun handleIncomingCall(call: Call) {
        val phoneNumber = call.details.handle?.schemeSpecificPart ?: ""
        val callerName = call.details.contactDisplayName ?: phoneNumber

        // Check if number is blocked
        if (blockManager.isNumberBlocked(phoneNumber)) {
            // Auto-reject blocked calls
            call.reject(false, "Number blocked")
            removeCall(call)
            return
        }

        // Show notification and start ringtone
        notificationManager.showIncomingCallNotification(callerName, phoneNumber)
        ringtoneManager.startRinging()
    }

    override fun answerCall() {
        ringtoneManager.stopRinging()
        notificationManager.cancelCallNotification()

        _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
            call.answer(0)
        }
    }

    override fun rejectCall() {
        ringtoneManager.stopRinging()
        notificationManager.cancelCallNotification()

        _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
            call.reject(false, "User rejected")
        }
        _currentCallState.value = CallState()
    }

    override fun endCall() {
        ringtoneManager.stopRinging()
        notificationManager.cancelCallNotification()

        _activeCalls.value.firstOrNull()?.let { call ->
            call.disconnect()
        }
        _currentCallState.value = CallState()
    }

    override fun holdCall() {
        _activeCalls.value.firstOrNull { it.state == Call.STATE_ACTIVE }?.let { call ->
            call.hold()
        }
    }

    override fun unholdCall() {
        _activeCalls.value.firstOrNull { it.state == Call.STATE_HOLDING }?.let { call ->
            call.unhold()
        }
    }

    override fun muteCall(muted: Boolean) {
        val currentState = _currentCallState.value
        _currentCallState.value = currentState.copy(isMuted = muted)
    }

    override fun blockNumber(phoneNumber: String): Boolean {
        return blockManager.blockNumber(phoneNumber)
    }

    override fun unblockNumber(phoneNumber: String): Boolean {
        return blockManager.unblockNumber(phoneNumber)
    }

    override fun isNumberBlocked(phoneNumber: String): Boolean {
        return blockManager.isNumberBlocked(phoneNumber)
    }

    override fun getBlockedNumbers(): List<String> {
        return blockManager.getBlockedNumbersList()
    }

    @OptIn(ExperimentalTime::class)
    private fun updateCurrentCallState() {
        val activeCall = _activeCalls.value.firstOrNull()

        if (activeCall != null) {
            val details = activeCall.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: _currentCallState.value.phoneNumber
            val contactName = details.contactDisplayName ?: ""

            val callState = CallState(
                isActive = activeCall.state == Call.STATE_ACTIVE,
                phoneNumber = phoneNumber,
                contactName = contactName.ifEmpty { phoneNumber },
                isIncoming = details.callDirection == Call.Details.DIRECTION_INCOMING,
                isRinging = activeCall.state == Call.STATE_RINGING,
                isOnHold = activeCall.state == Call.STATE_HOLDING,
                isConnecting = activeCall.state == Call.STATE_CONNECTING || activeCall.state == Call.STATE_DIALING,
                isMuted = _currentCallState.value.isMuted,
                callStartTime = if (activeCall.state == Call.STATE_ACTIVE && _currentCallState.value.callStartTime == null) {
                    kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                } else _currentCallState.value.callStartTime
            )
            _currentCallState.value = callState
        } else {
            if (!_currentCallState.value.isConnecting) {
                _currentCallState.value = CallState()
            }
        }
    }
}