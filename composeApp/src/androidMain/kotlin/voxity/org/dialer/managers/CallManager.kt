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
import kotlin.time.ExperimentalTime

class CallManager private constructor(private val context: Context) : CallRepository {

    private val TAG = "CallManager"

    private val _activeCalls = MutableStateFlow<List<Call>>(emptyList())
    val activeCalls: StateFlow<List<Call>> = _activeCalls.asStateFlow()

    private val _activeConnections = MutableStateFlow<List<Connection>>(emptyList())
    val activeConnections: StateFlow<List<Connection>> = _activeConnections.asStateFlow()

    private val _currentCallState = MutableStateFlow(CallState())
    override val currentCallState: StateFlow<CallState> = _currentCallState.asStateFlow()

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
        // Set connecting state immediately when call is initiated
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
        updateCurrentCallState()
    }

    fun removeCall(call: Call) {
        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.remove(call)
        _activeCalls.value = currentCalls
        updateCurrentCallState()
    }

    fun updateCallState(call: Call, state: Int) {
        updateCurrentCallState()
    }

    fun updateCallDetails(call: Call, details: Call.Details) {
        updateCurrentCallState()
    }

    fun addConnection(connection: Connection) {
        val currentConnections = _activeConnections.value.toMutableList()
        currentConnections.add(connection)
        _activeConnections.value = currentConnections
        // Update call state when connection is added
        updateCurrentCallState()
    }

    fun updateConnectionState(connection: Connection, state: Int) {
        // Update call state based on connection state
        updateCurrentCallState()
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
            // Only reset if we're not in connecting state
            if (!_currentCallState.value.isConnecting) {
                _currentCallState.value = CallState()
            }
        }
    }

    override fun answerCall() {
        _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
            call.answer(0)
        }
    }

    override fun rejectCall() {
        _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
            call.reject(false, "User rejected")
        }
        // Reset state after rejection
        _currentCallState.value = CallState()
    }

    override fun endCall() {
        _activeCalls.value.firstOrNull()?.let { call ->
            call.disconnect()
        }
        // Reset state after ending call
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
}