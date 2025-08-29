package io.voxity.dialer.services

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import io.voxity.dialer.domain.repository.CallRepository
import io.voxity.dialer.notifications.CallNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DialerInCallService : InCallService(), KoinComponent {

    private val TAG = "DialerInCallService"
    private val callRepository: CallRepository by inject()

    private val notificationManager: CallNotificationManager by inject()

    // Create proper coroutine scope
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callCallback)
        callRepository.onCallAdded(call)
        launchInCallUI(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callCallback)
        callRepository.onCallRemoved(call)
    }

    override fun onSilenceRinger() {
        super.onSilenceRinger()
        Log.d(TAG, "onSilenceRinger() called")
        callRepository.silenceRinger()
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            callRepository.onCallStateChanged()

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
            callRepository.onCallDetailsChanged()
        }
    }

    private fun launchInCallUI(call: Call) {
        Log.d(TAG, "Launching in-call UI")

        // Get the main activity class name from package
        val intent = Intent().apply {
            setClassName(packageName, "io.voxity.dialer.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("incoming_call", true)
            putExtra("call_handle", call.details.handle?.schemeSpecificPart)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch UI", e)
            // Fallback to notification if activity launch fails
            val phoneNumber = call.details.handle?.schemeSpecificPart ?: ""
            val callerName = call.details.contactDisplayName ?: phoneNumber
            notificationManager.showIncomingCallNotification(callerName, phoneNumber)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}