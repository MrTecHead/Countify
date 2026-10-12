package com.example.countify.data

import android.content.Context
import android.content.SharedPreferences
import com.example.countify.model.CounterItem
import com.example.countify.util.VibrantColors
import org.json.JSONArray
import java.util.Calendar

class CounterRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAllCounters(): List<CounterItem> {
        val jsonString = prefs.getString(KEY_COUNTERS, null) ?: return getSampleCounters()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<CounterItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(CounterItem.fromJsonObject(obj))
            }
            list
        } catch (e: Exception) {
            getSampleCounters()
        }
    }

    fun getCounterById(id: String): CounterItem? {
        return getAllCounters().find { it.id == id }
    }

    fun saveCounter(counter: CounterItem) {
        val currentList = getAllCounters().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == counter.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = counter
        } else {
            currentList.add(counter)
        }
        persistList(currentList)
    }

    fun deleteCounter(id: String) {
        val currentList = getAllCounters().filterNot { it.id == id }
        persistList(currentList)
    }

    private fun persistList(list: List<CounterItem>) {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it.toJsonObject()) }
        prefs.edit().putString(KEY_COUNTERS, jsonArray.toString()).apply()
    }

    private fun getSampleCounters(): List<CounterItem> {
        val now = Calendar.getInstance()

        // Create 2 sample default counters if empty
        val c1 = CounterItem(
            id = "sample_1",
            title = "New Year's Day",
            year = now.get(Calendar.YEAR) + 1,
            month = Calendar.JANUARY,
            dayOfMonth = 1,
            textColor = 0xFFFFFFFF.toInt(),
            backgroundColor = VibrantColors.BACKGROUND_COLORS[0]
        )

        val c2 = CounterItem(
            id = "sample_2",
            title = "Summer Vacation",
            year = now.get(Calendar.YEAR),
            month = Calendar.AUGUST,
            dayOfMonth = 15,
            textColor = 0xFFFFFFFF.toInt(),
            backgroundColor = VibrantColors.BACKGROUND_COLORS[6]
        )

        val samples = listOf(c1, c2)
        persistList(samples)
        return samples
    }

    companion object {
        private const val PREFS_NAME = "countify_prefs"
        private const val KEY_COUNTERS = "key_counters_list"

        @Volatile
        private var INSTANCE: CounterRepository? = null

        fun getInstance(context: Context): CounterRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CounterRepository(context).also { INSTANCE = it }
            }
        }
    }
}
