package com.mahesh.sparrow.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mahesh.sparrow.MainActivity
import com.mahesh.sparrow.R
import com.mahesh.sparrow.receiver.SparrowActionReceiver

/**
 * Owns the single required foreground-service notification: title/text,
 * and pause/resume, stop, and settings actions. This notification is never
 * hidden while Sparrow is running — see [NotificationCompat.Builder.setOngoing].
 */
class SparrowNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(isPaused: Boolean): Notification {
        val pauseResumeAction = if (isPaused) {
            NotificationCompat.Action(
                0,
                context.getString(R.string.notification_action_resume),
                actionPendingIntent(SparrowActionReceiver.ACTION_RESUME, REQUEST_PAUSE_RESUME)
            )
        } else {
            NotificationCompat.Action(
                0,
                context.getString(R.string.notification_action_pause),
                actionPendingIntent(SparrowActionReceiver.ACTION_PAUSE, REQUEST_PAUSE_RESUME)
            )
        }

        val stopAction = NotificationCompat.Action(
            0,
            context.getString(R.string.notification_action_stop),
            actionPendingIntent(SparrowActionReceiver.ACTION_STOP, REQUEST_STOP)
        )

        val settingsAction = NotificationCompat.Action(
            0,
            context.getString(R.string.notification_action_settings),
            settingsPendingIntent()
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_sparrow)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(settingsPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .addAction(settingsAction)
            .build()
    }

    private fun actionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, SparrowActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun settingsPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_SETTINGS, true)
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN_SETTINGS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "sparrow_status"
        const val NOTIFICATION_ID = 1001
        private const val REQUEST_PAUSE_RESUME = 1
        private const val REQUEST_STOP = 2
        private const val REQUEST_OPEN_SETTINGS = 3
    }
}
