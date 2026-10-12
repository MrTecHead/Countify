package com.example.countify.model

import com.example.countify.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CounterItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val year: Int,
    val month: Int, // 0-based (Calendar.JANUARY = 0)
    val dayOfMonth: Int,
    val textColor: Int,
    val backgroundColor: Int,
    val iconName: String = "calendar",
    val notificationType: String = NOTIF_NONE,
    val customDaysBefore: Int = 1,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0
) {

    fun getIconResId(): Int {
        return when (iconName) {
            "cake" -> R.drawable.ic_counter_cake
            "star" -> R.drawable.ic_counter_star
            "heart" -> R.drawable.ic_counter_heart
            "flight" -> R.drawable.ic_counter_flight
            "work" -> R.drawable.ic_counter_work
            "party" -> R.drawable.ic_counter_party
            "gift" -> R.drawable.ic_counter_gift
            "fitness" -> R.drawable.ic_counter_fitness
            "music" -> R.drawable.ic_counter_music
            else -> R.drawable.ic_counter_calendar
        }
    }

    fun getDaysLeft(nowMillis: Long = System.currentTimeMillis()): Long {
        val todayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    fun getDaysLeftText(nowMillis: Long = System.currentTimeMillis()): String {
        val days = getDaysLeft(nowMillis)
        return when {
            days > 1 -> "$days days left"
            days == 1L -> "1 day left"
            days == 0L -> "0 days left"
            days == -1L -> "1 day ago"
            else -> "${-days} days ago"
        }
    }

    fun getFormattedDate(): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
        return formatter.format(cal.time)
    }

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("year", year)
            put("month", month)
            put("dayOfMonth", dayOfMonth)
            put("textColor", textColor)
            put("backgroundColor", backgroundColor)
            put("iconName", iconName)
            put("notificationType", notificationType)
            put("customDaysBefore", customDaysBefore)
            put("notificationHour", notificationHour)
            put("notificationMinute", notificationMinute)
        }
    }

    companion object {
        const val NOTIF_NONE = "NONE"
        const val NOTIF_SAME_DAY = "SAME_DAY"
        const val NOTIF_ONE_DAY_BEFORE = "ONE_DAY_BEFORE"
        const val NOTIF_ONE_WEEK_BEFORE = "ONE_WEEK_BEFORE"
        const val NOTIF_CUSTOM = "CUSTOM"

        fun fromJsonObject(json: JSONObject): CounterItem {
            return CounterItem(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", ""),
                year = json.optInt("year", Calendar.getInstance().get(Calendar.YEAR)),
                month = json.optInt("month", Calendar.getInstance().get(Calendar.MONTH)),
                dayOfMonth = json.optInt("dayOfMonth", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)),
                textColor = json.optInt("textColor", 0xFFFFFFFF.toInt()),
                backgroundColor = json.optInt("backgroundColor", 0xFFFF5252.toInt()),
                iconName = json.optString("iconName", "calendar"),
                notificationType = json.optString("notificationType", NOTIF_NONE),
                customDaysBefore = json.optInt("customDaysBefore", 1),
                notificationHour = json.optInt("notificationHour", 9),
                notificationMinute = json.optInt("notificationMinute", 0)
            )
        }
    }
}
