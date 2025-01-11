package com.example.simplecount

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView

class NumberInputActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_input)

        // Load persisted values
        SimpleCount.loadFromPreferences(this)

        val inputField = findViewById<EditText>(R.id.number_input)
        val targetField = findViewById<EditText>(R.id.target_input) // The new target input field
        val submitButton = findViewById<Button>(R.id.btn_submit)
        val resetButton = findViewById<Button>(R.id.btn_reset) // Reset button

        // Set the default target value in the target input field
        targetField.setText(SimpleCount.target.toString())

        // Automatically focus on the number_input field and show the keyboard
        inputField.requestFocus()
        inputField.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(inputField, InputMethodManager.SHOW_IMPLICIT)
        }
        inputField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == 6) { // Replace 6 with the correct ID if different
                submitButton.performClick()
                true
            } else {
                false
            }
        }

        updateHistoryDisplay(this) // Make sure this is here to display the history


        submitButton.setOnClickListener {
            val inputText = inputField.text.toString()
            val targetText = targetField.text.toString()

            if (inputText.isNotBlank() && targetText.isNotBlank()) {
                val number = inputText.toIntOrNull()
                val target = targetText.toIntOrNull()

                if (number != null && target != null) {
                    // Update total and target
                    updateWidgetTotal(this, number, target)

                    // Update history
                    val history = loadHistory(this).toMutableList()
                    history.add(0, number.toString()) // Add the number to the top of the history
                    saveHistory(this, history)

                    // Update the history display in the activity
                    updateHistoryDisplay(this)

                    finish() // Close the activity
                } else {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Input cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        resetButton.setOnClickListener {
            resetWidgetTotal(this)

            // Reset history
            saveHistory(this, emptyList()) // Clear the history

            // Update the history display in the activity
            updateHistoryDisplay(this)

            finish() // Close the activity
        }

    }

    private fun updateWidgetTotal(context: Context, number: Int, target: Int) {
        SimpleCount.total += number
        SimpleCount.target = target
        SimpleCount.diff = target - SimpleCount.total

        // Save the updated values to preferences
        SimpleCount.saveToPreferences(context)

        // Notify the widget to update
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, SimpleCount::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        val intent = Intent(context, SimpleCount::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }

        // Send the broadcast to update the widget
        context.sendBroadcast(intent)
    }

    private fun resetWidgetTotal(context: Context) {
        SimpleCount.total = 0
        SimpleCount.diff = SimpleCount.target
        SimpleCount.saveToPreferences(context) // Save to disk
        notifyWidgetUpdate(context)
    }

    private fun notifyWidgetUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, SimpleCount::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        val intent = Intent(context, SimpleCount::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }

    private fun loadHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(SimpleCount.PREFS_NAME, Context.MODE_PRIVATE)
        val historyString = prefs.getString("history", "")
        return historyString?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    private fun saveHistory(context: Context, history: List<String>) {
        val prefs = context.getSharedPreferences(SimpleCount.PREFS_NAME, Context.MODE_PRIVATE)
        val historyString = history.joinToString(",")
        prefs.edit().putString("history", historyString).apply()
    }

    private fun updateHistoryDisplay(context: Context) {
        val history = loadHistory(context)
        val historyText = findViewById<TextView>(R.id.history_list)
        historyText.text = history.joinToString("\n")
    }
}
