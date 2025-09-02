package io.voxity.dialer.services

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
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
        Log.d(TAG, "Launching in-call UI for call direction: ${call.details.callDirection}")

        if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
            val phoneNumber = call.details.handle?.schemeSpecificPart ?: ""
            val callerName = call.details.contactDisplayName ?: phoneNumber

            notificationManager.showIncomingCallNotification(callerName, phoneNumber)

            launchUIActivity(call)
        } else {
            Log.d(TAG, "Outgoing call - no notification needed")
        }
    }

    private fun launchUIActivity(call: Call) {
        // Extract UI launching logic here to avoid duplication
        val appPackageName = applicationContext.packageName

        val activityClassName = try {
            val serviceInfo = packageManager.getServiceInfo(
                ComponentName(this, DialerInCallService::class.java),
                PackageManager.GET_META_DATA
            )
            serviceInfo.metaData?.getString("dialer.INCALL_ACTIVITY")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read metadata", e)
            null
        }

        if (activityClassName.isNullOrBlank()) {
            Log.d(TAG, "No specific activity configured, broadcasting intent")

            val broadcastIntent = Intent("io.voxity.dialer.INCOMING_CALL").apply {
                putExtra("incoming_call", true)
                putExtra("call_handle", call.details.handle?.schemeSpecificPart)
                putExtra("caller_name", call.details.contactDisplayName)
                setPackage(appPackageName)
            }
            sendBroadcast(broadcastIntent)
            return
        }

        val intent = Intent().apply {
            setClassName(appPackageName, activityClassName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("incoming_call", call.details.callDirection == Call.Details.DIRECTION_INCOMING)
            putExtra("call_handle", call.details.handle?.schemeSpecificPart)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch UI activity: $activityClassName", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}