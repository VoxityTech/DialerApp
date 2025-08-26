package voxity.org.dialer.services

import android.content.Intent
import android.os.IBinder
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import voxity.org.dialer.MainActivity
import voxity.org.dialer.managers.CallManager
import org.json.JSONObject

class MyInCallService : InCallService() {

    private val TAG = "MyInCallService"
    private val LIFECYCLE_TAG = "InCallServiceLifecycle"
    private val callManager by lazy { CallManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        Log.d(LIFECYCLE_TAG, "============ InCallService CREATED ============")
        Log.d(LIFECYCLE_TAG, "Service instance created at: ${System.currentTimeMillis()}")
        Log.d(LIFECYCLE_TAG, "==============================================")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(LIFECYCLE_TAG, "============ InCallService BIND ============")
        Log.d(LIFECYCLE_TAG, "Bind intent: ${intent?.toString()}")
        Log.d(LIFECYCLE_TAG, "Bind action: ${intent?.action}")
        Log.d(LIFECYCLE_TAG, "Bind extras: ${intent?.extras}")
        Log.d(LIFECYCLE_TAG, "Timestamp: ${System.currentTimeMillis()}")

        val binder = super.onBind(intent)
        Log.d(LIFECYCLE_TAG, "Binder returned: ${binder != null}")
        Log.d(LIFECYCLE_TAG, "==========================================")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(LIFECYCLE_TAG, "============ InCallService UNBIND ============")
        Log.d(LIFECYCLE_TAG, "Unbind intent: ${intent?.toString()}")
        Log.d(LIFECYCLE_TAG, "Timestamp: ${System.currentTimeMillis()}")

        val result = super.onUnbind(intent)
        Log.d(LIFECYCLE_TAG, "Unbind result: $result")
        Log.d(LIFECYCLE_TAG, "============================================")
        return result
    }

    override fun onDestroy() {
        Log.d(LIFECYCLE_TAG, "============ InCallService DESTROYED ============")
        Log.d(LIFECYCLE_TAG, "Service destroyed at: ${System.currentTimeMillis()}")
        super.onDestroy()
        Log.d(LIFECYCLE_TAG, "===============================================")
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d(TAG, "============ CALL ADDED TO INCALL SERVICE ============")
        Log.d(TAG, "Call handle: ${call.details.handle}")
        Log.d(TAG, "Call state: ${call.state}")
        Log.d(TAG, "Call direction: ${call.details.callDirection}")
        Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")

        // Log complete call details
        logCallAddedDetails(call)

        // Register callback to monitor call state changes
        call.registerCallback(callCallback)
        callManager.addCall(call)
        launchInCallUI(call)

        Log.d(TAG, "====================================================")
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d(TAG, "============ CALL REMOVED FROM INCALL SERVICE ============")
        Log.d(TAG, "Call handle: ${call.details.handle}")
        Log.d(TAG, "Final call state: ${call.state}")
        Log.d(TAG, "Call duration: ${System.currentTimeMillis() - call.details.creationTimeMillis}ms")
        Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")

        // Unregister callback
        call.unregisterCallback(callCallback)

        // Update call manager
        callManager.removeCall(call)

        Log.d(TAG, "========================================================")
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
        Log.d(TAG, "============ CAN ADD CALL CHANGED ============")
        Log.d(TAG, "Can add call: $canAddCall")
        Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")
        Log.d(TAG, "=============================================")
    }

    override fun onSilenceRinger() {
        super.onSilenceRinger()
        Log.d(TAG, "============ SILENCE RINGER ============")
        Log.d(TAG, "Ringer silenced by user")
        Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")
        Log.d(TAG, "=======================================")
    }

    override fun onConnectionEvent(call: Call, event: String, extras: android.os.Bundle?) {
        super.onConnectionEvent(call, event, extras)
        Log.d(TAG, "============ CONNECTION EVENT ============")
        Log.d(TAG, "Call: ${call.details.handle}")
        Log.d(TAG, "Event: $event")
        Log.d(TAG, "Extras: $extras")
        Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")
        Log.d(TAG, "=========================================")
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d(TAG, "============ CALL STATE CHANGED CALLBACK ============")
            Log.d(TAG, "Call: ${call.details.handle?.schemeSpecificPart}")
            Log.d(TAG, "Previous state -> New state: $state")
            Log.d(TAG, "State name: ${getStateString(state)}")
            Log.d(TAG, "Timestamp: ${System.currentTimeMillis()}")

            callManager.updateCallState(call, state)

            when (state) {
                Call.STATE_RINGING -> {
                    Log.d(TAG, "Call is ringing - checking direction")
                    if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
                        Log.d(TAG, "Incoming call ringing - launching UI")
                        launchInCallUI(call)
                    } else {
                        Log.d(TAG, "Outgoing call ringing")
                    }
                }
                Call.STATE_ACTIVE -> {
                    Log.d(TAG, "Call became active")
                    logCallActiveMetadata(call)
                }
                Call.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Call disconnected")
                    logCallDisconnectedMetadata(call)
                }
                else -> {
                    Log.d(TAG, "Call state: ${getStateString(state)}")
                }
            }
            Log.d(TAG, "===================================================")
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            Log.d(TAG, "============ CALL DETAILS CHANGED CALLBACK ============")
            Log.d(TAG, "Call: ${details.handle?.schemeSpecificPart}")
            logDetailedCallMetadata(call, details, "DETAILS_CHANGED")
            callManager.updateCallDetails(call, details)
            Log.d(TAG, "=====================================================")
        }

        override fun onPostDialWait(call: Call, remainingPostDialSequence: String) {
            super.onPostDialWait(call, remainingPostDialSequence)
            Log.d(TAG, "============ POST DIAL WAIT ============")
            Log.d(TAG, "Call: ${call.details.handle?.schemeSpecificPart}")
            Log.d(TAG, "Remaining sequence: $remainingPostDialSequence")
            Log.d(TAG, "======================================")
        }

        override fun onVideoCallChanged(call: Call, videoCall: InCallService.VideoCall?) {
            super.onVideoCallChanged(call, videoCall)
            Log.d(TAG, "============ VIDEO CALL CHANGED ============")
            Log.d(TAG, "Call: ${call.details.handle?.schemeSpecificPart}")
            Log.d(TAG, "Video call: ${videoCall != null}")
            Log.d(TAG, "==========================================")
        }

        override fun onCallDestroyed(call: Call) {
            super.onCallDestroyed(call)
            Log.d(TAG, "============ CALL DESTROYED CALLBACK ============")
            Log.d(TAG, "Call: ${call.details.handle?.schemeSpecificPart}")
            Log.d(TAG, "Call destroyed at: ${System.currentTimeMillis()}")
            Log.d(TAG, "===============================================")
        }

        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: List<Call>) {
            super.onConferenceableCallsChanged(call, conferenceableCalls)
            Log.d(TAG, "============ CONFERENCEABLE CALLS CHANGED ============")
            Log.d(TAG, "Call: ${call.details.handle?.schemeSpecificPart}")
            Log.d(TAG, "Conferenceable calls count: ${conferenceableCalls.size}")
            Log.d(TAG, "====================================================")
        }
    }

    private fun launchInCallUI(call: Call) {
        Log.d(TAG, "============ LAUNCHING IN-CALL UI ============")
        Log.d(TAG, "Call: ${call.details.handle?.schemeSpecificPart}")
        Log.d(TAG, "Call state: ${getStateString(call.state)}")
        Log.d(TAG, "Call direction: ${getDirectionString(call.details.callDirection)}")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_IN_CALL", true)
            putExtra("CALL_ID", call.details.handle?.schemeSpecificPart ?: "")
            putExtra("CALL_STATE", call.state)
            putExtra("CALL_DIRECTION", call.details.callDirection)
        }

        Log.d(TAG, "Starting MainActivity with in-call intent")
        startActivity(intent)
        Log.d(TAG, "=============================================")
    }

    // Detailed logging methods
    private fun logCallAddedDetails(call: Call) {
        val metadata = JSONObject().apply {
            put("event", "CALL_ADDED")
            put("timestamp", System.currentTimeMillis())
            put("phoneNumber", call.details.handle?.schemeSpecificPart ?: "Unknown")
            put("contactName", call.details.contactDisplayName ?: "Unknown")
            put("callDirection", getDirectionString(call.details.callDirection))
            put("callState", getStateString(call.state))
            put("accountHandle", call.details.accountHandle?.toString() ?: "Unknown")
            put("creationTime", call.details.creationTimeMillis)
            put("connectTime", call.details.connectTimeMillis)
            put("callCapabilities", call.details.callCapabilities)
            put("callProperties", call.details.callProperties)
            put("videoState", call.details.videoState)
        }
        Log.d("CallAddedMetadata", metadata.toString())
    }

    private fun logCallActiveMetadata(call: Call) {
        Log.d("CallActiveMetadata", "============ CALL ACTIVE METADATA ============")
        Log.d("CallActiveMetadata", "Phone: ${call.details.handle?.schemeSpecificPart}")
        Log.d("CallActiveMetadata", "Connect time: ${call.details.connectTimeMillis}")
        Log.d("CallActiveMetadata", "Creation time: ${call.details.creationTimeMillis}")
        Log.d("CallActiveMetadata", "Call duration so far: ${call.details.connectTimeMillis - call.details.creationTimeMillis}ms")
        Log.d("CallActiveMetadata", "==============================================")
    }

    private fun logCallDisconnectedMetadata(call: Call) {
        Log.d("CallDisconnectedMetadata", "============ CALL DISCONNECTED METADATA ============")
        Log.d("CallDisconnectedMetadata", "Phone: ${call.details.handle?.schemeSpecificPart}")
        Log.d("CallDisconnectedMetadata", "Total call duration: ${System.currentTimeMillis() - call.details.creationTimeMillis}ms")
        Log.d("CallDisconnectedMetadata", "Disconnect cause: ${call.details.disconnectCause}")
        Log.d("CallDisconnectedMetadata", "===================================================")
    }

    private fun logDetailedCallMetadata(call: Call, details: Call.Details, event: String) {
        val metadata = JSONObject().apply {
            put("event", event)
            put("timestamp", System.currentTimeMillis())
            put("phoneNumber", details.handle?.schemeSpecificPart ?: "Unknown")
            put("contactName", details.contactDisplayName ?: "Unknown")
            put("callerDisplayName", details.callerDisplayName ?: "Unknown")
            put("callDirection", getDirectionString(details.callDirection))
            put("callState", getStateString(call.state))
            put("accountHandle", details.accountHandle?.toString() ?: "Unknown")
            put("creationTime", details.creationTimeMillis)
            put("connectTime", details.connectTimeMillis)
            put("callCapabilities", details.callCapabilities)
            put("callProperties", details.callProperties)
            put("videoState", details.videoState)
            put("gatewayInfo", details.gatewayInfo?.toString() ?: "None")
            put("intentExtras", details.intentExtras?.toString() ?: "None")
            put("statusHints", details.statusHints?.toString() ?: "None")
            put("disconnectCause", details.disconnectCause?.toString() ?: "None")
        }
        Log.d("DetailedCallMetadata", metadata.toString())
    }

    // Helper methods
    private fun getStateString(state: Int): String {
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

    private fun getDirectionString(direction: Int): String {
        return when (direction) {
            Call.Details.DIRECTION_INCOMING -> "INCOMING"
            Call.Details.DIRECTION_OUTGOING -> "OUTGOING"
            Call.Details.DIRECTION_UNKNOWN -> "UNKNOWN"
            else -> "UNDEFINED($direction)"
        }
    }
}