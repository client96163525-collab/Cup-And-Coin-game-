package com.example.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    const val APP_WEBSITE_URL = "https://cupandcoin.vercel.app"

    fun shareText(context: Context, text: String, chooserTitle: String = "Share Cup and Coin") {
        try {
            // Ensure website link is always attached if not already included
            val fullText = if (!text.contains(APP_WEBSITE_URL)) {
                "$text\n\n🌐 Play / Download Game:\n$APP_WEBSITE_URL"
            } else {
                text
            }

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, fullText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

