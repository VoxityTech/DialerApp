package io.voxity.dialer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.voxity.dialer.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CallActionReceiver : BroadcastReceiver(), KoinComponent {

    private val callRepository: CallRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        when (intent?.action) {
            "ACTION_ANSWER_CALL" -> {
                scope.launch {
                    callRepository.answerCall()
                }
            }
            "ACTION_REJECT_CALL" -> {
                scope.launch {
                    callRepository.rejectCall()
                }
            }
        }
    }
}