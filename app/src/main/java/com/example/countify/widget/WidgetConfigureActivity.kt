package com.example.countify.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.countify.data.CounterRepository
import com.example.countify.databinding.ActivityWidgetConfigureBinding
import com.example.countify.ui.CounterAdapter

class WidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var binding: ActivityWidgetConfigureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        binding = ActivityWidgetConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        val repository = CounterRepository.getInstance(this)

        // Check if counter_id was passed directly in extras or options bundle
        val directCounterId = extras?.getString("counter_id")
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && !directCounterId.isNullOrEmpty()) {
            val directCounter = repository.getCounterById(directCounterId)
            if (directCounter != null) {
                CounterWidgetProvider.saveWidgetCounterId(this, appWidgetId, directCounter.id)
                val appWidgetManager = AppWidgetManager.getInstance(this)
                CounterWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, directCounter)

                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()
                return
            }
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val counters = repository.getAllCounters()

        val adapter = CounterAdapter(
            onItemClick = { counter ->
                CounterWidgetProvider.saveWidgetCounterId(this, appWidgetId, counter.id)
                val appWidgetManager = AppWidgetManager.getInstance(this)
                CounterWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, counter)

                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            },
            onItemLongClick = {}
        )

        binding.rvCountersConfigure.layoutManager = LinearLayoutManager(this)
        binding.rvCountersConfigure.adapter = adapter
        adapter.submitList(counters)
    }
}
