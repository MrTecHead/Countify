package com.example.countify.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.countify.data.CounterRepository

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val counterId = intent.getStringExtra(EXTRA_COUNTER_ID) ?: return
        val repository = CounterRepository.getInstance(context)
        val counter = repository.getCounterById(counterId) ?: return

        val daysLeftText = counter.getDaysLeftText()
        val title = counter.title
        val contentText = "$title: $daysLeftText (${counter.getFormattedDate()})"

        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showNotification(context, counter.id.hashCode(), title, contentText)
    }

    companion object {
        const val EXTRA_COUNTER_ID = "extra_counter_id"
    }
}
