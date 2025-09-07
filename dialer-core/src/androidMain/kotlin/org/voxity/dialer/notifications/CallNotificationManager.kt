package org.voxity.dialer.notifications

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.voxity.dialer.domain.models.DialerConfig
import org.voxity.dialer.receivers.CallActionReceiver

class CallNotificationManager(
    private val context: Context,
    private val config: DialerConfig
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CALL_NOTIFICATION_ID = 1001

    private val ONGOING_CALL_NOTIFICATION_ID = 1002

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                config.notificationChannelId,
                config.notificationChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
                setSound(null, null)
                enableVibration(config.enableVibration)
                if (config.enableVibration) {
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOngoingCallNotification(
        callerName: String,
        callerNumber: String,
        targetActivityClass: Class<*>
    ) {
        val intent = Intent(context, targetActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("return_to_call", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, config.notificationChannelId)
            .setContentTitle("Ongoing call")
            .setContentText("$callerName • Tap to return")
            .setSmallIcon(R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(ONGOING_CALL_NOTIFICATION_ID, notification)
    }

    fun showIncomingCallNotification(
        callerName: String,
        callerNumber: String,
        targetActivityClass: Class<*>? = null
    ) {
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

        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_ANSWER_CALL"
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context, 1, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_REJECT_CALL"
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context, 2, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, config.notificationChannelId)
            .setContentTitle("Incoming call")
            .setContentText("$callerName ($callerNumber)")
            .setSmallIcon(R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_menu_close_clear_cancel,
                "Reject",
                rejectPendingIntent
            )
            .addAction(
                R.drawable.ic_menu_call,
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