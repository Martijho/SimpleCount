package com.example.simplecount

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class SimpleCount : AppWidgetProvider() {

    companion object {
        const val PREFS_NAME = "SimpleCountPrefs"
        const val KEY_TOTAL = "key_total"
        const val KEY_TARGET = "key_target"
        const val KEY_DIFF = "key_diff"

        var total = 0
        var target = 2000
        var diff = 2000

        fun saveToPreferences(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putInt(KEY_TOTAL, total)
                putInt(KEY_TARGET, target)
                putInt(KEY_DIFF, diff)
                apply() // Save asynchronously
            }
        }

        fun loadFromPreferences(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            total = prefs.getInt(KEY_TOTAL, 0)
            target = prefs.getInt(KEY_TARGET, 2000)
            diff = prefs.getInt(KEY_DIFF, 2000)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Load saved data from preferences
        loadFromPreferences(context)

        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, NumberInputActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate the progress percentage
            val progress = if (target > 0) (total * 100 / target) else 0

            // Create RemoteViews to update the widget UI
            val views = RemoteViews(context.packageName, R.layout.count_simple).apply {
                setOnClickPendingIntent(R.id.progress_bar, pendingIntent) // Make the ProgressBar clickable
                setTextViewText(R.id.txt_total, "Total: $total") // Update total
                setTextViewText(R.id.txt_diff, "Diff: ${target - total}") // Update diff
                setProgressBar(R.id.progress_bar, 100, progress, false) // Update progress bar
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }


}
