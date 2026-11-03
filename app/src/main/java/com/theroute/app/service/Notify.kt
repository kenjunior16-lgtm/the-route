package com.theroute.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object Notify {
    const val CHANNEL = "route_channel"
    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL) == null) {
            mgr.createNotificationChannel(NotificationChannel(CHANNEL, "The Route", NotificationManager.IMPORTANCE_HIGH))
        }
    }
    fun show(context: Context, title: String, text: String) {
        ensureChannel(context)
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()
        mgr.notify((System.currentTimeMillis() % 100000).toInt(), n)
    }
}
