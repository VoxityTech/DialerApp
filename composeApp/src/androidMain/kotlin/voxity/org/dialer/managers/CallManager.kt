package voxity.org.dialer.managers

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.Call
import android.telecom.Connection
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.json.JSONObject
import kotlin.time.ExperimentalTime

class CallManager private constructor() {

    private val TAG = "CallManager"
    private val METADATA_TAG = "CallMetadata"

    private lateinit var context: Context

    private val _activeCalls = MutableStateFlow<List<Call>>(emptyList())
    val activeCalls: StateFlow<List<Call>> = _activeCalls.asStateFlow()

    private val _activeConnections = MutableStateFlow<List<Connection>>(emptyList())
    val activeConnections: StateFlow<List<Connection>> = _activeConnections.asStateFlow()

    private val _currentCallState = MutableStateFlow(voxity.org.dialer.models.CallState())
    val currentCallState: StateFlow<voxity.org.dialer.models.CallState> = _currentCallState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: CallManager? = null

        fun getInstance(context: Context? = null): CallManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CallManager().also {
                    it.context = context?.applicationContext
                        ?: throw IllegalArgumentException("Context required for first initialization")
                    INSTANCE = it
                }
            }
        }
    }

    fun addCall(call: Call) {
        Log.d(TAG, "============ CALL ADDED ============")
        logCallDetails(call, "CALL_ADDED")

        // Add enhanced metadata logging
        logAllAvailableMetadata(call, "CALL_ADDED_ENHANCED")

        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.add(call)
        _activeCalls.value = currentCalls
        updateCurrentCallState()

        // Log system telephony info
        logSystemTelephonyInfo()

        Log.d(TAG, "Total active calls: ${_activeCalls.value.size}")
        Log.d(TAG, "====================================")
    }

    fun removeCall(call: Call) {
        Log.d(TAG, "============ CALL REMOVED ============")
        logCallDetails(call, "CALL_REMOVED")

        val currentCalls = _activeCalls.value.toMutableList()
        currentCalls.remove(call)
        _activeCalls.value = currentCalls
        updateCurrentCallState()

        Log.d(TAG, "Total active calls: ${_activeCalls.value.size}")
        Log.d(TAG, "======================================")
    }

    fun updateCallState(call: Call, state: Int) {
        Log.d(TAG, "============ CALL STATE CHANGED ============")
        Log.d(TAG, "New State: ${getCallStateString(state)}")
        logCallDetails(call, "CALL_STATE_CHANGED")
        updateCurrentCallState()
        Log.d(TAG, "===========================================")
    }

    fun updateCallDetails(call: Call, details: Call.Details) {
        Log.d(TAG, "============ CALL DETAILS CHANGED ============")
        logCallDetailsExtensive(call, details, "CALL_DETAILS_CHANGED")
        updateCurrentCallState()
        Log.d(TAG, "=============================================")
    }

    fun addConnection(connection: Connection) {
        Log.d(TAG, "============ CONNECTION ADDED ============")
        logConnectionDetails(connection, "CONNECTION_ADDED")

        val currentConnections = _activeConnections.value.toMutableList()
        currentConnections.add(connection)
        _activeConnections.value = currentConnections

        Log.d(TAG, "Total active connections: ${_activeConnections.value.size}")
        Log.d(TAG, "=========================================")
    }

    fun updateConnectionState(connection: Connection, state: Int) {
        Log.d(TAG, "============ CONNECTION STATE CHANGED ============")
        Log.d(TAG, "Connection State: ${getConnectionStateString(state)}")
        logConnectionDetails(connection, "CONNECTION_STATE_CHANGED")
        Log.d(TAG, "=================================================")
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun logAllAvailableMetadata(call: Call, event: String) {
        Log.d(METADATA_TAG, "========== $event - ALL METADATA ==========")

        val details = call.details

        // Basic call info
        Log.d(METADATA_TAG, "Call Handle: ${details.handle}")
        Log.d(METADATA_TAG, "Account Handle: ${details.accountHandle}")

        // Try to extract telecom call ID (if available)
        try {
            val callIdField = details.javaClass.getDeclaredField("mTelecomCallId")
            callIdField.isAccessible = true
            val telecomCallId = callIdField.get(details) as? String
            Log.d(METADATA_TAG, "Telecom Call ID: $telecomCallId")
        } catch (e: Exception) {
            Log.d(METADATA_TAG, "Could not extract Telecom Call ID: ${e.message}")
        }

        // Extract all intent extras
        val intentExtras = details.intentExtras
        if (intentExtras != null) {
            Log.d(METADATA_TAG, "Intent Extras Keys: ${intentExtras.keySet()}")
            for (key in intentExtras.keySet()) {
                try {
                    val value = intentExtras.get(key)
                    Log.d(METADATA_TAG, "Extra [$key]: $value")

                    // Look for any bridge/channel related data
                    if (key.contains("bridge", ignoreCase = true) ||
                        key.contains("channel", ignoreCase = true) ||
                        key.contains("call_id", ignoreCase = true) ||
                        key.contains("session", ignoreCase = true)) {
                        Log.w(METADATA_TAG, "🔍 POTENTIAL ID DATA: [$key]: $value")
                    }
                } catch (e: Exception) {
                    Log.d(METADATA_TAG, "Extra [$key]: Could not extract - ${e.message}")
                }
            }
        }

        // Try to access all Call methods to find hidden metadata
        try {
            val callClass = call.javaClass
            val methods = callClass.methods
            for (method in methods) {
                if (method.name.startsWith("get") && method.parameterCount == 0) {
                    try {
                        val result = method.invoke(call)
                        Log.d(METADATA_TAG, "Call.${method.name}(): $result")
                    } catch (e: Exception) {
                        // Skip methods that throw exceptions
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(METADATA_TAG, "Error extracting call methods: ${e.message}")
        }

        // Gateway info
        val gatewayInfo = details.gatewayInfo
        if (gatewayInfo != null) {
            Log.d(METADATA_TAG, "Gateway Address: ${gatewayInfo.gatewayAddress}")
            Log.d(METADATA_TAG, "Original Address: ${gatewayInfo.originalAddress}")
            Log.d(METADATA_TAG, "Gateway Package: ${gatewayInfo.gatewayProviderPackageName}")
        }

        // Status hints
        val statusHints = details.statusHints
        if (statusHints != null) {
            Log.d(METADATA_TAG, "Status Hints Label: ${statusHints.label}")
            Log.d(METADATA_TAG, "Status Hints Icon: ${statusHints.icon}")
            Log.d(METADATA_TAG, "Status Hints Extras: ${statusHints.extras}")
        }

        Log.d(METADATA_TAG, "===========================================")
    }

    private fun logSystemTelephonyInfo() {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            Log.d(METADATA_TAG, "========== SYSTEM TELEPHONY INFO ==========")
            Log.d(METADATA_TAG, "Network Operator: ${telephonyManager.networkOperator}")
            Log.d(METADATA_TAG, "Network Operator Name: ${telephonyManager.networkOperatorName}")
            Log.d(METADATA_TAG, "SIM Operator: ${telephonyManager.simOperator}")
            Log.d(METADATA_TAG, "SIM Operator Name: ${telephonyManager.simOperatorName}")
            Log.d(METADATA_TAG, "Phone Type: ${telephonyManager.phoneType}")

            // Remove the getNetworkCapabilities() call - it doesn't exist
            // TelephonyManager doesn't have this method

            Log.d(METADATA_TAG, "==========================================")
        } catch (e: Exception) {
            Log.w(METADATA_TAG, "Error getting telephony info: ${e.message}")
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun updateCurrentCallState() {
        val activeCall = _activeCalls.value.firstOrNull()

        if (activeCall != null) {
            val details = activeCall.details
            val phoneNumber = details.handle?.schemeSpecificPart ?: ""
            val contactName = details.contactDisplayName ?: ""

            val callState = voxity.org.dialer.models.CallState(
                isActive = activeCall.state == Call.STATE_ACTIVE,
                phoneNumber = phoneNumber,
                contactName = contactName.ifEmpty { phoneNumber },
                isIncoming = details.callDirection == Call.Details.DIRECTION_INCOMING,
                isRinging = activeCall.state == Call.STATE_RINGING,
                isOnHold = activeCall.state == Call.STATE_HOLDING,
                isMuted = false,
                callStartTime = if (activeCall.state == Call.STATE_ACTIVE) {
                    kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                } else null
            )

            _currentCallState.value = callState
            Log.d(TAG, "Updated UI Call State: $callState")
        } else {
            _currentCallState.value = voxity.org.dialer.models.CallState()
            Log.d(TAG, "No active calls - UI state reset")
        }
    }

    fun answerCall() {
        Log.d(TAG, "============ ANSWERING CALL ============")
        _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
            Log.d(TAG, "Answering call to: ${call.details.handle?.schemeSpecificPart}")
            logCallDetails(call, "BEFORE_ANSWER")
            call.answer(0) // VideoProfile.STATE_AUDIO_ONLY
            Log.d(TAG, "Answer command sent")
        } ?: run {
            Log.w(TAG, "No ringing call to answer")
        }
        Log.d(TAG, "======================================")
    }

    fun rejectCall() {
        Log.d(TAG, "============ REJECTING CALL ============")
        _activeCalls.value.firstOrNull { it.state == Call.STATE_RINGING }?.let { call ->
            Log.d(TAG, "Rejecting call from: ${call.details.handle?.schemeSpecificPart}")
            logCallDetails(call, "BEFORE_REJECT")
            call.reject(false, "User rejected")
            Log.d(TAG, "Reject command sent")
        } ?: run {
            Log.w(TAG, "No ringing call to reject")
        }
        Log.d(TAG, "=======================================")
    }

    fun endCall() {
        Log.d(TAG, "============ ENDING CALL ============")
        _activeCalls.value.firstOrNull()?.let { call ->
            Log.d(TAG, "Ending call with: ${call.details.handle?.schemeSpecificPart}")
            logCallDetails(call, "BEFORE_END")
            call.disconnect()
            Log.d(TAG, "Disconnect command sent")
        } ?: run {
            Log.w(TAG, "No active call to end")
        }
        Log.d(TAG, "====================================")
    }

    fun holdCall() {
        Log.d(TAG, "============ HOLDING CALL ============")
        _activeCalls.value.firstOrNull { it.state == Call.STATE_ACTIVE }?.let { call ->
            Log.d(TAG, "Holding call with: ${call.details.handle?.schemeSpecificPart}")
            logCallDetails(call, "BEFORE_HOLD")
            call.hold()
            Log.d(TAG, "Hold command sent")
        } ?: run {
            Log.w(TAG, "No active call to hold")
        }
        Log.d(TAG, "=====================================")
    }

    fun unholdCall() {
        Log.d(TAG, "============ UNHOLDING CALL ============")
        _activeCalls.value.firstOrNull { it.state == Call.STATE_HOLDING }?.let { call ->
            Log.d(TAG, "Unholding call with: ${call.details.handle?.schemeSpecificPart}")
            logCallDetails(call, "BEFORE_UNHOLD")
            call.unhold()
            Log.d(TAG, "Unhold command sent")
        } ?: run {
            Log.w(TAG, "No held call to unhold")
        }
        Log.d(TAG, "=======================================")
    }

    fun muteCall(mute: Boolean) {
        Log.d(TAG, "============ ${if (mute) "MUTING" else "UNMUTING"} CALL ============")
        Log.d(TAG, "Mute state: $mute")
        val currentState = _currentCallState.value
        _currentCallState.value = currentState.copy(isMuted = mute)
        Log.d(TAG, "UI mute state updated")
        Log.d(TAG, "===============================================")
    }

    // Detailed logging methods
    private fun logCallDetails(call: Call, event: String) {
        Log.d(METADATA_TAG, "========== $event ==========")
        Log.d(METADATA_TAG, "Call State: ${getCallStateString(call.state)}")
        Log.d(METADATA_TAG, "Phone Number: ${call.details.handle?.schemeSpecificPart ?: "Unknown"}")
        Log.d(METADATA_TAG, "Contact Name: ${call.details.contactDisplayName ?: "Unknown"}")
        Log.d(METADATA_TAG, "Call Direction: ${getCallDirectionString(call.details.callDirection)}")
        Log.d(METADATA_TAG, "Timestamp: ${System.currentTimeMillis()}")

        // Basic call details
        val details = call.details
        Log.d(METADATA_TAG, "Account Handle: ${details.accountHandle}")
        Log.d(METADATA_TAG, "Call Capabilities: ${getCallCapabilitiesString(details.callCapabilities)}")
        Log.d(METADATA_TAG, "Call Properties: ${getCallPropertiesString(details.callProperties)}")
        Log.d(METADATA_TAG, "Connect Time: ${details.connectTimeMillis}")
        Log.d(METADATA_TAG, "Creation Time: ${details.creationTimeMillis}")
        Log.d(METADATA_TAG, "Has Video: ${call.details.videoState != 0}")
        Log.d(METADATA_TAG, "Video State: ${details.videoState}")

        Log.d(METADATA_TAG, "==========================================")
    }

    private fun logCallDetailsExtensive(call: Call, details: Call.Details, event: String) {
        Log.d(METADATA_TAG, "========== $event ==========")

        // Create comprehensive metadata JSON
        val metadata = JSONObject().apply {
            put("event", event)
            put("timestamp", System.currentTimeMillis())
            put("callState", getCallStateString(call.state))
            put("phoneNumber", details.handle?.schemeSpecificPart ?: "Unknown")
            put("contactName", details.contactDisplayName ?: "Unknown")
            put("callDirection", getCallDirectionString(details.callDirection))
            put("accountHandle", details.accountHandle?.toString() ?: "Unknown")
            put("callCapabilities", details.callCapabilities)
            put("callProperties", details.callProperties)
            put("connectTime", details.connectTimeMillis)
            put("creationTime", details.creationTimeMillis)
            put("videoState", details.videoState)
            put("hasVideo", details.videoState != 0)

            // Additional details
            put("callerDisplayName", details.callerDisplayName ?: "Unknown")
            put("callerDisplayNamePresentation", details.callerDisplayNamePresentation)
            put("gatewayInfo", details.gatewayInfo?.toString() ?: "None")
            put("intentExtras", details.intentExtras?.toString() ?: "None")
            put("statusHints", details.statusHints?.toString() ?: "None")
        }

        Log.d(METADATA_TAG, "Complete Call Metadata: $metadata")
        Log.d(METADATA_TAG, "==========================================")
    }

    private fun logConnectionDetails(connection: Connection, event: String) {
        Log.d(METADATA_TAG, "========== $event ==========")
        Log.d(METADATA_TAG, "Connection State: ${getConnectionStateString(connection.state)}")
        Log.d(METADATA_TAG, "Connection Address: ${connection.address}")
        Log.d(METADATA_TAG, "Connection Capabilities: ${connection.connectionCapabilities}")
        Log.d(METADATA_TAG, "Connection Properties: ${connection.connectionProperties}")
        Log.d(METADATA_TAG, "Audio Mode VoIP: ${connection.audioModeIsVoip}")
        Log.d(METADATA_TAG, "Caller Display Name: ${connection.callerDisplayName}")
        Log.d(METADATA_TAG, "Timestamp: ${System.currentTimeMillis()}")
        Log.d(METADATA_TAG, "==========================================")
    }

    // Helper methods for readable logging
    private fun getCallStateString(state: Int): String {
        return when (state) {
            Call.STATE_NEW -> "NEW"
            Call.STATE_DIALING -> "DIALING"
            Call.STATE_RINGING -> "RINGING"
            Call.STATE_HOLDING -> "HOLDING"
            Call.STATE_ACTIVE -> "ACTIVE"
            Call.STATE_DISCONNECTED -> "DISCONNECTED"
            Call.STATE_CONNECTING -> "CONNECTING"
            Call.STATE_DISCONNECTING -> "DISCONNECTING"
            Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
            Call.STATE_SIMULATED_RINGING -> "SIMULATED_RINGING"
            Call.STATE_AUDIO_PROCESSING -> "AUDIO_PROCESSING"
            else -> "UNKNOWN($state)"
        }
    }

    private fun getConnectionStateString(state: Int): String {
        return when (state) {
            Connection.STATE_INITIALIZING -> "INITIALIZING"
            Connection.STATE_NEW -> "NEW"
            Connection.STATE_RINGING -> "RINGING"
            Connection.STATE_DIALING -> "DIALING"
            Connection.STATE_ACTIVE -> "ACTIVE"
            Connection.STATE_HOLDING -> "HOLDING"
            Connection.STATE_DISCONNECTED -> "DISCONNECTED"
            else -> "UNKNOWN($state)"
        }
    }

    private fun getCallDirectionString(direction: Int): String {
        return when (direction) {
            Call.Details.DIRECTION_INCOMING -> "INCOMING"
            Call.Details.DIRECTION_OUTGOING -> "OUTGOING"
            Call.Details.DIRECTION_UNKNOWN -> "UNKNOWN"
            else -> "UNDEFINED($direction)"
        }
    }

    private fun getCallCapabilitiesString(capabilities: Int): String {
        val caps = mutableListOf<String>()
        if (capabilities and Call.Details.CAPABILITY_HOLD != 0) caps.add("HOLD")
        if (capabilities and Call.Details.CAPABILITY_SUPPORT_HOLD != 0) caps.add("SUPPORT_HOLD")
        if (capabilities and Call.Details.CAPABILITY_MERGE_CONFERENCE != 0) caps.add("MERGE_CONFERENCE")
        if (capabilities and Call.Details.CAPABILITY_SWAP_CONFERENCE != 0) caps.add("SWAP_CONFERENCE")
        if (capabilities and Call.Details.CAPABILITY_RESPOND_VIA_TEXT != 0) caps.add("RESPOND_VIA_TEXT")
        if (capabilities and Call.Details.CAPABILITY_MUTE != 0) caps.add("MUTE")
        if (capabilities and Call.Details.CAPABILITY_MANAGE_CONFERENCE != 0) caps.add("MANAGE_CONFERENCE")
        if (capabilities and Call.Details.CAPABILITY_SUPPORTS_VT_LOCAL_RX != 0) caps.add("VT_LOCAL_RX")
        if (capabilities and Call.Details.CAPABILITY_SUPPORTS_VT_LOCAL_TX != 0) caps.add("VT_LOCAL_TX")
        if (capabilities and Call.Details.CAPABILITY_SUPPORTS_VT_REMOTE_RX != 0) caps.add("VT_REMOTE_RX")
        if (capabilities and Call.Details.CAPABILITY_SUPPORTS_VT_REMOTE_TX != 0) caps.add("VT_REMOTE_TX")
        return caps.joinToString(", ").ifEmpty { "NONE" }
    }

    private fun getCallPropertiesString(properties: Int): String {
        val props = mutableListOf<String>()
        if (properties and Call.Details.PROPERTY_CONFERENCE != 0) props.add("CONFERENCE")
        if (properties and Call.Details.PROPERTY_GENERIC_CONFERENCE != 0) props.add("GENERIC_CONFERENCE")
        if (properties and Call.Details.PROPERTY_WIFI != 0) props.add("WIFI")
        if (properties and Call.Details.PROPERTY_HIGH_DEF_AUDIO != 0) props.add("HIGH_DEF_AUDIO")
        if (properties and Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE != 0) props.add("EMERGENCY_CALLBACK")
        if (properties and Call.Details.PROPERTY_ENTERPRISE_CALL != 0) props.add("ENTERPRISE")
        return props.joinToString(", ").ifEmpty { "NONE" }
    }
}