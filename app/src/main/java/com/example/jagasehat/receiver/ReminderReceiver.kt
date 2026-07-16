package com.example.jagasehat.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.jagasehat.MainActivity
import com.example.jagasehat.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Pengingat Kesehatan"
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""
        val reminderId = intent.getStringExtra(EXTRA_ID) ?: title
        showNotification(context, reminderId, title, time)
    }

    private fun showNotification(context: Context, reminderId: String, title: String, time: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat JagaSehat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi jadwal kesehatan keluarga"
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktunya: $title")
            .setContentText(if (time.isNotBlank()) "Jadwal pukul $time" else "Jadwal kesehatan aktif")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(reminderId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "jagasehat_reminder_channel"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TIME = "extra_time"
        const val EXTRA_ID = "extra_id"
    }
}
