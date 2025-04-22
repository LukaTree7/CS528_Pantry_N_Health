package com.example.afinal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.afinal.MainActivity
import com.example.afinal.R
import com.example.afinal.data.FoodItem
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "expiry_notification_channel"
    private const val NOTIFICATION_ID = 1

    private const val GROUP_KEY_EXPIRY = "com.example.afinal.EXPIRING_ITEMS"

    private const val SUMMARY_NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val name = "Food Expiry Notifications"
        val descriptionText = "Notifications for food items about to expire"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showExpiryNotification(context: Context, expiringItems: List<FoodItem>) {
        if (expiringItems.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val itemNames = expiringItems.joinToString(", ") { it.name }
        val contentText = if (expiringItems.size == 1) {
            "${expiringItems[0].name} is expiring soon!"
        } else {
            "$itemNames are expiring soon!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Food Items Expiring Soon")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {

            }
        }
    }

    fun showSmartExpiryNotifications(context: Context, expiringItems: List<FoodItem>) {
        if (expiringItems.isEmpty()) return

        val notificationManager = NotificationManagerCompat.from(context)
        val today = Calendar.getInstance().time


        val groupedByDays = expiringItems.groupBy { item ->
            val daysBetween = ((item.expiryDate.time - today.time) /
                    TimeUnit.DAYS.toMillis(1)).toInt()
            daysBetween
        }


        var notificationId = NOTIFICATION_ID + 100

        groupedByDays.forEach { (daysRemaining, items) ->
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context, daysRemaining, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val itemNames = items.joinToString(", ") { it.name }
            val timeframeText = when {
                daysRemaining == 0 -> "today"
                daysRemaining == 1 -> "tomorrow"
                else -> "in $daysRemaining days"
            }

            val title = if (items.size == 1) {
                "${items[0].name} expires $timeframeText"
            } else {
                "${items.size} items expire $timeframeText"
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(itemNames)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_KEY_EXPIRY)
                .setAutoCancel(true)

            if (items.size > 1) {
                val style = NotificationCompat.InboxStyle()
                    .setBigContentTitle(title)

                items.forEach { item ->
                    style.addLine(item.name)
                }

                builder.setStyle(style)
            }

            try {
                notificationManager.notify(notificationId++, builder.build())
            } catch (e: SecurityException) {
                Log.e("Notification", "Permission denied", e)
            }
        }

        if (groupedByDays.size > 1) {
            val summaryBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Food Expiry Alert")
                .setContentText("${expiringItems.size} items expiring soon")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(GROUP_KEY_EXPIRY)
                .setGroupSummary(true)
                .setAutoCancel(true)

            try {
                notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryBuilder.build())
            } catch (e: SecurityException) {
                Log.e("Notification", "Permission denied", e)
            }
        }
    }

    fun shouldNotifyForItem(item: FoodItem): Boolean {
        val today = Calendar.getInstance().time
        val daysBetween = ((item.expiryDate.time - today.time) /
                TimeUnit.DAYS.toMillis(1)).toInt()

        if (daysBetween < 0) return false

        val shelfLifeDays = ((item.expiryDate.time - item.dateAdded.time) /
                TimeUnit.DAYS.toMillis(1)).toInt()

        return when {

            shelfLifeDays <= 3 -> daysBetween <= 1

            shelfLifeDays <= 14 -> daysBetween <= 3

            else -> daysBetween <= 7
        }
    }
}