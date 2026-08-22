package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object AppInstallReporter {
    private const val TAG = "AppInstallReporter"
    private const val PREF_NAME = "app_install_prefs"
    private const val KEY_REPORTED = "is_install_reported"
    private const val KEY_USERNAME = "app_username"
    private const val API_URL = "https://cupandcoin.vercel.app/api/installs"

    fun reportInstallIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val isReported = prefs.getBoolean(KEY_REPORTED, false)
        
        var username = prefs.getString(KEY_USERNAME, null)
        if (username.isNullOrEmpty()) {
            val randomNum = Random.nextInt(1000, 9999)
            val androidModels = listOf("Pixel", "Galaxy", "OnePlus", "Redmi", "Moto", "Vivo", "Oppo", "Realme")
            val modelPrefix = androidModels.random()
            username = "${modelPrefix}_User_$randomNum"
            prefs.edit().putString(KEY_USERNAME, username).apply()
        }

        if (!isReported) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val url = URL(API_URL)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                        doOutput = true
                        connectTimeout = 10000
                        readTimeout = 10000
                    }

                    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    val jsonPayload = """
                        {
                            "username": "$username",
                            "device": "$deviceName",
                            "version": "1.0.4",
                            "date": "$currentDate"
                        }
                    """.trimIndent()

                    conn.outputStream.use { os ->
                        OutputStreamWriter(os, "UTF-8").use { writer ->
                            writer.write(jsonPayload)
                            writer.flush()
                        }
                    }

                    val responseCode = conn.responseCode
                    if (responseCode in 200..299) {
                        prefs.edit().putBoolean(KEY_REPORTED, true).apply()
                        Log.i(TAG, "Successfully reported app install for $username to website!")
                    } else {
                        Log.w(TAG, "Failed to report install, response code: $responseCode")
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    Log.e(TAG, "Error reporting app install: ${e.message}", e)
                }
            }
        }
    }
}
