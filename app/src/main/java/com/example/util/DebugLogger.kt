package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel { INFO, DEBUG, WARN, ERROR }

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String
)

object DebugLogger {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _isConsoleVisible = MutableStateFlow(false)
    val isConsoleVisible: StateFlow<Boolean> = _isConsoleVisible.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun toggleConsole() {
        _isConsoleVisible.value = !_isConsoleVisible.value
    }

    fun showConsole() {
        _isConsoleVisible.value = true
    }

    fun hideConsole() {
        _isConsoleVisible.value = false
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun log(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            level = level,
            tag = tag,
            message = message
        )

        // Log to Android Logcat
        when (level) {
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }

        // Add to live in-app log list
        val current = _logs.value
        _logs.value = (listOf(entry) + current).take(150)
    }

    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARN, tag, message)
    fun e(tag: String, message: String) = log(LogLevel.ERROR, tag, message)
}
