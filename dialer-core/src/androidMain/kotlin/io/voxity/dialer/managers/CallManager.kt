package io.voxity.dialer.managers

import android.content.Context
import android.os.Build
import android.telecom.Call
import android.telecom.Connection
import android.util.Log
import io.voxity.dialer.audio.CallAudioManager
import io.voxity.dialer.audio.CallRingtoneManager
import io.voxity.dialer.blocking.ContactBlockManager
import io.voxity.dialer.notifications.CallNotificationManager
import io.voxity.dialer.domain.models.DialerConfig
import io.voxity.dialer.domain.models.CallResult
import io.voxity.dialer.domain.models.DialerResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import io.voxity.dialer.domain.models.CallState
import io.voxity.dialer.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallManager(
    private val context: Context,
    private val config: DialerConfig,
    private val audioManager: CallAudioManager,
    val ringtoneManager: CallRingtoneManager,
    private val notificationManager: CallNotificationManager,
    private val blockManager: ContactBlockManager
) : CallRepository {

    private val TAG = "CallManager"

    private val managerScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "Coroutine exception", exception)
        }
    )

    private var currentMuteState = false

    init {
        // Sync initial mute state
        audioManager.syncMuteState()

        // Observe mute state changes
        managerScope.launch {
            audioManager.isMuted.collect { isMuted ->
                currentMuteState = isMuted
                updateCurrentCallState()
            }
        }
    }

    private val _activeCalls = MutableStateFlow<List<Call>>(emptyList())
    val activeCalls: StateFlow<List<Call>> = _activeCalls.asStateFlow()

    private val _activeConnections = MutableStateFlow<List<Connection>>(emptyList())
    val activeConnections: StateFlow<List<Connection>> = _activeConnections.asStateFlow()

    private val _currentCallState = MutableStateFlow(CallState())
    override val currentCallState: StateFlow<CallState> = _currentCallState.asStateFlow()

    override suspend fun makeCall(phoneNumber: String): CallResult = withContext(Dispatchers.IO) {
        try {
            val isBlocked = blockManager.isNumberBlocked(phoneNumber)
            if (isBlocked is DialerResult.Success && isBlocked.data) {
                return@withContext CallResult.Error("Number is blocked")
            }

            _currentCallState.value = CallState(
                isConnecting = true,
                phoneNumber = phoneNumber,
                contactName = phoneNumber,
                isIncoming = false
            )

            io.voxity.dialer.makeCall(phoneNumber)
            CallResult.Success
        } catch (e: Exception) {
            CallResult.Error("Failed to make call", e)
        }
    }

    fun addCall(call: Call) {
        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.add(call)
        _activeCalls.value = currentCalls

        if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                handleIncomingCall(call)
            }
        }

        updateCurrentCallState()
    }

    fun removeCall(call: Call) {
        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.remove(call)
        _activeCalls.value = currentCalls

        ringtoneManager.stopRinging()
        notificationManager.cancelCallNotification()

        updateCurrentCallState()
    }

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

    private suspend fun handleIncomingCall(call: Call) = withContext(Dispatchers.IO) {
        val phoneNumber = call.details.handle?.schemeSpecificPart ?: ""
        val callerName = call.details.contactDisplayName ?: phoneNumber

        val isBlockedResult = blockManager.isNumberBlocked(phoneNumber)
        if (isBlockedResult is DialerResult.Success && isBlockedResult.data) {
            call.reject(false, "Number blocked")
            removeCall(call)
            return@withContext
        }

        notificationManager.showIncomingCallNotification(callerName, phoneNumber)
        if (config.enableRingtone) {
            ringtoneManager.startRinging()
        }
    }

    override suspend fun answerCall(): CallResult = withContext(Dispatchers.Main) {
        try {
            ringtoneManager.stopRinging()
            notificationManager.cancelCallNotification()

            _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
                call.answer(0)
                CallResult.Success
            } ?: CallResult.Error("No ringing call found")
        } catch (e: Exception) {
            CallResult.Error("Failed to answer call", e)
        }
    }

    override suspend fun rejectCall(): CallResult = withContext(Dispatchers.Main) {
        try {
            ringtoneManager.stopRinging()
            notificationManager.cancelCallNotification()

            _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
                call.reject(false, "User rejected")
                _currentCallState.value = CallState()
                CallResult.Success
            } ?: CallResult.Error("No ringing call found")
        } catch (e: Exception) {
            CallResult.Error("Failed to reject call", e)
        }
    }

    override suspend fun endCall(): CallResult = withContext(Dispatchers.Main) {
        try {
            ringtoneManager.stopRinging()
            notificationManager.cancelCallNotification()

            _activeCalls.value.firstOrNull()?.let { call ->
                call.disconnect()
                _currentCallState.value = CallState()
                CallResult.Success
            } ?: CallResult.Error("No active call found")
        } catch (e: Exception) {
            CallResult.Error("Failed to end call", e)
        }
    }

    override suspend fun holdCall(): CallResult = withContext(Dispatchers.Main) {
        try {
            _activeCalls.value.firstOrNull { it.state == Call.STATE_ACTIVE }?.let { call ->
                call.hold()
                updateCurrentCallState()
                CallResult.Success
            } ?: CallResult.Error("No active call to hold")
        } catch (e: Exception) {
            CallResult.Error("Failed to hold call", e)
        }
    }

    override suspend fun unholdCall(): CallResult = withContext(Dispatchers.Main) {
        try {
            _activeCalls.value.firstOrNull { it.state == Call.STATE_HOLDING }?.let { call ->
                call.unhold()
                updateCurrentCallState()
                CallResult.Success
            } ?: CallResult.Error("No call on hold")
        } catch (e: Exception) {
            CallResult.Error("Failed to unhold call", e)
        }
    }

    override suspend fun muteCall(muted: Boolean): CallResult {
        return audioManager.setMute(muted)
    }

    override suspend fun blockNumber(phoneNumber: String): DialerResult<Boolean> = withContext(Dispatchers.IO) {
        blockManager.blockNumber(phoneNumber)
    }

    override suspend fun unblockNumber(phoneNumber: String): DialerResult<Boolean> = withContext(Dispatchers.IO) {
        blockManager.unblockNumber(phoneNumber)
    }

    override suspend fun isNumberBlocked(phoneNumber: String): DialerResult<Boolean> = withContext(Dispatchers.IO) {
        blockManager.isNumberBlocked(phoneNumber)
    }

    override suspend fun getBlockedNumbers(): DialerResult<List<String>> = withContext(Dispatchers.IO) {
        blockManager.getBlockedNumbers()
    }

    // Add these implementations to CallManager
    override fun onCallAdded(call: Any) {
        if (call is Call && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            addCall(call)
        }
    }

    override fun onCallRemoved(call: Any) {
        if (call is Call) {
            removeCall(call)
        }
    }

    override fun onCallStateChanged() {
        updateCallState()
    }

    override fun onCallDetailsChanged() {
        updateCallDetails()
    }

    override fun silenceRinger() {
        ringtoneManager.silenceRinging()
    }

    fun cleanup() {
        managerScope.cancel()
        ringtoneManager.stopRinging()
        notificationManager.cancelCallNotification()
        managerScope.launch {
            audioManager.abandonAudioFocus()
        }
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
                isMuted = currentMuteState,
                callStartTime = if (activeCall.state == Call.STATE_ACTIVE && _currentCallState.value.callStartTime == null) {
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                } else _currentCallState.value.callStartTime
            )
            _currentCallState.value = callState

            // Use coroutine scope for async operations
            if (callState.isActive && config.enableAudioFocus) {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    audioManager.requestAudioFocus()
                }
            } else if (!callState.isRinging && !callState.isOnHold && !callState.isConnecting) {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    audioManager.abandonAudioFocus()
                }
            }
        } else {
            val currentState = _currentCallState.value
            if (!currentState.isConnecting) {
                _currentCallState.value = CallState()
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    audioManager.abandonAudioFocus()
                }
            }
        }
    }
}