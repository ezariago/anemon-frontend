package me.ezar.anemon.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.ezar.anemon.MainActivity
import me.ezar.anemon.R

object NotificationHelper {

    private const val CHANNEL_ID = "AnemonServiceChannel"
    const val FOREGROUND_NOTIFICATION_ID = 1
    private const val EVENT_NOTIFICATION_ID = 2 // buat match requests/found

    fun createNotificationChannel(context: Context) {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Anemon Background Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createPendingIntent(context: Context): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun buildForegroundNotification(context: Context, text: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Anemon")
            .setContentText(text)
            .setSmallIcon(R.drawable.baseline_two_wheeler_24)
            .setContentIntent(createPendingIntent(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun showMatchRequestNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Permintaan Tumpangan Baru!")
            .setContentText("Ada yang rute perjalanannya mirip nih sama kamu!")
            .setSmallIcon(R.drawable.baseline_hail_24)
            .setContentIntent(createPendingIntent(context))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(EVENT_NOTIFICATION_ID, notification)
    }

    fun showMatchFoundNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Dapat Tumpangan!")
            .setContentText("Ada yang terima perjalananmu nih!")
            .setSmallIcon(R.drawable.baseline_directions_car_24)
            .setContentIntent(createPendingIntent(context))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(EVENT_NOTIFICATION_ID, notification)
    }
}