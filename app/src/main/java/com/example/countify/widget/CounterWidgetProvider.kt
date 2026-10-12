package com.example.countify.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.countify.MainActivity
import com.example.countify.R
import com.example.countify.data.CounterRepository
import com.example.countify.model.CounterItem

class CounterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {

        private const val PREFS_WIDGET_MAP = "prefs_widget_map"
        private const val KEY_WIDGET_PREFIX = "widget_counter_"

        fun saveWidgetCounterId(context: Context, appWidgetId: Int, counterId: String) {
            val prefs = context.getSharedPreferences(PREFS_WIDGET_MAP, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_WIDGET_PREFIX + appWidgetId, counterId).apply()
        }

        fun getWidgetCounterId(context: Context, appWidgetId: Int): String? {
            val prefs = context.getSharedPreferences(PREFS_WIDGET_MAP, Context.MODE_PRIVATE)
            return prefs.getString(KEY_WIDGET_PREFIX + appWidgetId, null)
        }

        private fun getTintedBitmap(context: Context, resId: Int, color: Int): Bitmap? {
            return try {
                val drawable = ContextCompat.getDrawable(context, resId)?.mutate() ?: return null
                DrawableCompat.setTint(drawable, color)
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 48
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 48
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } catch (_: Exception) {
                null
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            counter: CounterItem? = null
        ) {
            val repository = CounterRepository.getInstance(context)

            val options = try { appWidgetManager.getAppWidgetOptions(appWidgetId) } catch (_: Exception) { null }
            val optionsCounterId = options?.getString("counter_id")
            if (!optionsCounterId.isNullOrEmpty()) {
                saveWidgetCounterId(context, appWidgetId, optionsCounterId)
            }

            val selectedCounter = counter
                ?: getWidgetCounterId(context, appWidgetId)?.let { repository.getCounterById(it) }
                ?: repository.getAllCounters().firstOrNull()

            val views = RemoteViews(context.packageName, R.layout.widget_counter)

            if (selectedCounter != null) {
                views.setTextViewText(R.id.widget_tv_title, selectedCounter.title)
                views.setTextViewText(R.id.widget_tv_date, selectedCounter.getFormattedDate())
                views.setTextViewText(R.id.widget_tv_days_left, selectedCounter.getDaysLeftText())

                views.setTextColor(R.id.widget_tv_title, selectedCounter.textColor)
                views.setTextColor(R.id.widget_tv_date, selectedCounter.textColor)
                views.setTextColor(R.id.widget_tv_days_left, selectedCounter.textColor)

                val bitmap = getTintedBitmap(context, selectedCounter.getIconResId(), selectedCounter.textColor)
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_iv_icon, bitmap)
                } else {
                    views.setImageViewResource(R.id.widget_iv_icon, selectedCounter.getIconResId())
                }

                views.setInt(
                    R.id.widget_root,
                    "setBackgroundColor",
                    selectedCounter.backgroundColor
                )
            } else {
                views.setTextViewText(R.id.widget_tv_title, "Countify")
                views.setTextViewText(R.id.widget_tv_date, "No counter")
                views.setTextViewText(R.id.widget_tv_days_left, "--")
                val bitmap = getTintedBitmap(context, R.drawable.ic_counter_calendar, 0xFFFFFFFF.toInt())
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_iv_icon, bitmap)
                } else {
                    views.setImageViewResource(R.id.widget_iv_icon, R.drawable.ic_counter_calendar)
                }
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CounterWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun requestPinWidget(context: Context, counter: CounterItem): Boolean {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CounterWidgetProvider::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {
                val extras = Bundle().apply {
                    putString("counter_id", counter.id)
                }

                val pinnedWidgetDataIntent = Intent(context, CounterWidgetProvider::class.java).apply {
                    action = "com.example.countify.PIN_WIDGET_CALLBACK"
                    putExtra("counter_id", counter.id)
                }

                val successCallback = PendingIntent.getBroadcast(
                    context,
                    counter.id.hashCode(),
                    pinnedWidgetDataIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                appWidgetManager.requestPinAppWidget(componentName, extras, successCallback)
                return true
            }
            return false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.example.countify.PIN_WIDGET_CALLBACK") {
            val counterId = intent.getStringExtra("counter_id") ?: return
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                saveWidgetCounterId(context, appWidgetId, counterId)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val repository = CounterRepository.getInstance(context)
                val counter = repository.getCounterById(counterId)
                updateAppWidget(context, appWidgetManager, appWidgetId, counter)
            }
        }
    }
}
