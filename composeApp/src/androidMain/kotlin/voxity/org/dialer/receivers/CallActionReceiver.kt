package voxity.org.dialer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import voxity.org.dialer.managers.CallManager

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "ACTION_ANSWER_CALL" -> {
                CallManager.getInstance().answerCall()
            }
            "ACTION_REJECT_CALL" -> {
                CallManager.getInstance().rejectCall()
            }
        }
    }
}