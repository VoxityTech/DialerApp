package io.voxity.dialer.services

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.RequiresApi
import io.voxity.dialer.managers.CallManager

class DialerInCallService : InCallService() {

    private val TAG = "DialerInCallService"
    private val callManager by lazy { CallManager.create(this) }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callManager.addCall(call)
        }

        launchInCallUI(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        callManager.removeCall(call)
    }

    override fun onSilenceRinger() {
        super.onSilenceRinger()
        Log.d(TAG, "onSilenceRinger() called")
        callManager.ringtoneManager.silenceRinging()
    }

    private val callCallback = object : Call.Callback() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            callManager.updateCallState()

            when (state) {
                Call.STATE_RINGING -> {
                    if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
                        Log.d(TAG, "Incoming call ringing")
                        launchInCallUI(call)
                    }
                }
                Call.STATE_ACTIVE -> Log.d(TAG, "Call became active")
                Call.STATE_DISCONNECTED -> Log.d(TAG, "Call disconnected")
            }
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            callManager.updateCallDetails()
        }
    }

    private fun launchInCallUI(call: Call) {
        // This will be handled by the consuming app
        // The service shouldn't know about specific activities
        Log.d(TAG, "Call UI should be launched by consuming app")
    }
}