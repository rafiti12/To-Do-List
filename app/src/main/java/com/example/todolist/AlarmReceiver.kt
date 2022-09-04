package com.example.todolist

import android.R
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val title = intent.getStringExtra("title").toString()
        val i = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)
        val builder: NotificationCompat.Builder? = context?.let {
            NotificationCompat.Builder(it, "foxandroid")
                .setSmallIcon(R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText("Planned completion time for task is near.")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
        }
        val notificationManagerCompat = context?.let { NotificationManagerCompat.from(it) }
        if (builder != null) {
            if (notificationManagerCompat != null) {
                notificationManagerCompat.notify(123, builder.build())
            }
        }
    }
}