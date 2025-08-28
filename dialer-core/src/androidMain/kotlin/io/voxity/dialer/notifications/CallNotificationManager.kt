package io.voxity.dialer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.voxity.dialer.receivers.CallActionReceiver

class CallNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CALL_NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "incoming_calls"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showIncomingCallNotification(
        callerName: String,
        callerNumber: String,
        targetActivityClass: Class<*>? = null
    ) {
        // Create generic intent that can be overridden by consuming app
        val fullScreenIntent = targetActivityClass?.let { activityClass ->
            Intent(context, activityClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("incoming_call", true)
                putExtra("caller_name", callerName)
                putExtra("caller_number", callerNumber)
            }
        }

        val fullScreenPendingIntent = fullScreenIntent?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Answer intent - fixed syntax
        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_ANSWER_CALL"
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context, 1, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reject intent - fixed syntax
        val rejectIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_REJECT_CALL"
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context, 2, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Incoming call")
            .setContentText("$callerName ($callerNumber)")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Reject",
                rejectPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_call,
                "Answer",
                answerPendingIntent
            )
            .apply {
                fullScreenPendingIntent?.let { pendingIntent ->
                    setContentIntent(pendingIntent)
                    setFullScreenIntent(pendingIntent, true)
                }
            }
            .build()

        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    fun cancelCallNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }
}