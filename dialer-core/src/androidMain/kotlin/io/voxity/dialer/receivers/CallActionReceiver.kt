package io.voxity.dialer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.voxity.dialer.managers.CallManager

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        when (intent?.action) {
            "ACTION_ANSWER_CALL" -> {
                CallManager.create(context).answerCall()
            }
            "ACTION_REJECT_CALL" -> {
                CallManager.create(context).rejectCall()
            }
        }
    }
}