package com.example.countify.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.countify.MainActivity
import com.example.countify.R
import com.example.countify.model.CounterItem
import java.util.Calendar

object NotificationHelper {

    const val CHANNEL_ID = "countify_channel_id"
    private const val CHANNEL_NAME = "Countify Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for event countdowns"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, counter: CounterItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_COUNTER_ID, counter.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            counter.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (counter.notificationType == CounterItem.NOTIF_NONE) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val daysBefore = when (counter.notificationType) {
            CounterItem.NOTIF_SAME_DAY -> 0
            CounterItem.NOTIF_ONE_DAY_BEFORE -> 1
            CounterItem.NOTIF_ONE_WEEK_BEFORE -> 7
            CounterItem.NOTIF_CUSTOM -> counter.customDaysBefore
            else -> 0
        }

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, counter.year)
            set(Calendar.MONTH, counter.month)
            set(Calendar.DAY_OF_MONTH, counter.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, counter.notificationHour)
            set(Calendar.MINUTE, counter.notificationMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -daysBefore)
        }

        val triggerMillis = triggerCal.timeInMillis
        if (triggerMillis > System.currentTimeMillis()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun cancelNotification(context: Context, counterId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            counterId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    @Suppress("MissingPermission")
    fun showNotification(context: Context, notificationId: Int, title: String, content: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }
}
