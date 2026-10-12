package com.example.countify

import com.example.countify.model.CounterItem
import com.example.countify.util.VibrantColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CounterUnitTest {

    @Test
    fun testFormattedDate() {
        val counter = CounterItem(
            title = "Test Event",
            year = 2026,
            month = Calendar.AUGUST,
            dayOfMonth = 12,
            textColor = 0xFFFFFFFF.toInt(),
            backgroundColor = 0xFFFF5252.toInt()
        )

        assertEquals("12 August 2026", counter.getFormattedDate())
    }

    @Test
    fun testDaysLeftCalculation() {
        val baseCal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 1, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = baseCal.timeInMillis

        // Event in 11 days (August 12)
        val futureCounter = CounterItem(
            title = "Future Event",
            year = 2026,
            month = Calendar.AUGUST,
            dayOfMonth = 12,
            textColor = 0,
            backgroundColor = 0
        )
        assertEquals(11L, futureCounter.getDaysLeft(nowMillis))
        assertEquals("11 days left", futureCounter.getDaysLeftText(nowMillis))

        // Event today (August 1)
        val todayCounter = CounterItem(
            title = "Today Event",
            year = 2026,
            month = Calendar.AUGUST,
            dayOfMonth = 1,
            textColor = 0,
            backgroundColor = 0
        )
        assertEquals(0L, todayCounter.getDaysLeft(nowMillis))
        assertEquals("0 days left", todayCounter.getDaysLeftText(nowMillis))

        // Event 1 day in future (August 2)
        val tomorrowCounter = CounterItem(
            title = "Tomorrow Event",
            year = 2026,
            month = Calendar.AUGUST,
            dayOfMonth = 2,
            textColor = 0,
            backgroundColor = 0
        )
        assertEquals(1L, tomorrowCounter.getDaysLeft(nowMillis))
        assertEquals("1 day left", tomorrowCounter.getDaysLeftText(nowMillis))

        // Event 5 days in past (July 27)
        val pastCounter = CounterItem(
            title = "Past Event",
            year = 2026,
            month = Calendar.JULY,
            dayOfMonth = 27,
            textColor = 0,
            backgroundColor = 0
        )
        assertEquals(-5L, pastCounter.getDaysLeft(nowMillis))
        assertEquals("5 days ago", pastCounter.getDaysLeftText(nowMillis))
    }

    @Test
    fun testJsonSerialization() {
        val original = CounterItem(
            id = "test_uuid_123",
            title = "Vacation",
            year = 2027,
            month = Calendar.DECEMBER,
            dayOfMonth = 25,
            textColor = 0xFF000000.toInt(),
            backgroundColor = 0xFF00BCD4.toInt(),
            iconName = "cake",
            notificationType = CounterItem.NOTIF_ONE_DAY_BEFORE,
            customDaysBefore = 3,
            notificationHour = 8,
            notificationMinute = 30
        )

        val json = original.toJsonObject()
        val restored = CounterItem.fromJsonObject(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.year, restored.year)
        assertEquals(original.month, restored.month)
        assertEquals(original.dayOfMonth, restored.dayOfMonth)
        assertEquals(original.textColor, restored.textColor)
        assertEquals(original.backgroundColor, restored.backgroundColor)
        assertEquals(original.iconName, restored.iconName)
        assertEquals(original.notificationType, restored.notificationType)
        assertEquals(original.customDaysBefore, restored.customDaysBefore)
        assertEquals(original.notificationHour, restored.notificationHour)
        assertEquals(original.notificationMinute, restored.notificationMinute)
    }

    @Test
    fun testVibrantColorsPalette() {
        assertTrue(VibrantColors.BACKGROUND_COLORS.isNotEmpty())
        assertTrue(VibrantColors.TEXT_COLORS.isNotEmpty())

        val randomColor = VibrantColors.getRandomVibrantColor()
        assertTrue(VibrantColors.BACKGROUND_COLORS.contains(randomColor))
    }
}
