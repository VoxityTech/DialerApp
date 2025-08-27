package voxity.org.dialer.services

import android.content.Intent
import android.os.IBinder
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import voxity.org.dialer.MainActivity
import voxity.org.dialer.managers.CallManager

class MyInCallService : InCallService() {

    private val TAG = "MyInCallService"
    private val LIFECYCLE_TAG = "InCallServiceLifecycle"
    private val callManager by lazy { CallManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {

        val binder = super.onBind(intent)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        val result = super.onUnbind(intent)
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        // Register callback to monitor call state changes
        call.registerCallback(callCallback)
        callManager.addCall(call)
        launchInCallUI(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)

        // Update call manager
        callManager.removeCall(call)
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
    }

    override fun onSilenceRinger() {
        super.onSilenceRinger()
    }

    override fun onConnectionEvent(call: Call, event: String, extras: android.os.Bundle?) {
        super.onConnectionEvent(call, event, extras)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            callManager.updateCallState(call, state)

            when (state) {
                Call.STATE_RINGING -> {
                    if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
                        Log.d(TAG, "Incoming call ringing - launching UI")
                        launchInCallUI(call)
                    } else {
                        Log.d(TAG, "Outgoing call ringing")
                    }
                }

                Call.STATE_ACTIVE -> {
                    Log.d(TAG, "Call became active")
                }

                Call.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Call disconnected")
                }

                else -> {
                    Log.d(TAG, "Call state: ${getStateString(state)}")
                }
            }
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            callManager.updateCallDetails(call, details)
        }

        override fun onPostDialWait(call: Call, remainingPostDialSequence: String) {
            super.onPostDialWait(call, remainingPostDialSequence)
        }

        override fun onVideoCallChanged(call: Call, videoCall: InCallService.VideoCall?) {
            super.onVideoCallChanged(call, videoCall)
        }

        override fun onCallDestroyed(call: Call) {
            super.onCallDestroyed(call)
        }

        override fun onConferenceableCallsChanged(call: Call, conferenceableCalls: List<Call>) {
            super.onConferenceableCallsChanged(call, conferenceableCalls)
        }
    }

    private fun launchInCallUI(call: Call) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_IN_CALL", true)
            putExtra("CALL_ID", call.details.handle?.schemeSpecificPart ?: "")
            putExtra("CALL_STATE", call.state)
            putExtra("CALL_DIRECTION", call.details.callDirection)
        }
        startActivity(intent)
    }

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