package voxity.org.dialer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import voxity.org.dialer.managers.CallManager

class CallReceiver : BroadcastReceiver() {

    private val callManager = CallManager.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                when (state) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        // Incoming call
                        phoneNumber?.let {
                            // Handle incoming call
                        }
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        // Call answered
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        // Call ended
                    }
                }
            }

            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                // Handle outgoing call
            }
        }
    }
}