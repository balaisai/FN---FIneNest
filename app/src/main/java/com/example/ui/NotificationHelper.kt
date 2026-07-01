package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "finnest_alerts"
    private const val CHANNEL_NAME = "FinNest Wallet Alerts"
    private const val CHANNEL_DESC = "Notifications for transaction additions and daily financial summaries"

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = 0xFF0D9488.toInt() // TealPrimary color hex
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun postTransactionNotification(
        context: Context,
        type: String,
        amount: Double,
        category: String,
        payer: String,
        notes: String
    ) {
        val sharedPrefs = context.getSharedPreferences("vault_security_prefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean("notifications_transaction_enabled", true)
        if (!isEnabled) return

        createNotificationChannel(context)

        // Clean currency display
        val formattedAmount = try {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.format(amount)
        } catch (e: Exception) {
            "$${String.format("%.2f", amount)}"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val iconId = com.example.R.mipmap.ic_launcher
        val contentTitle = if (type.equals("Income", ignoreCase = true)) {
            "💰 +$formattedAmount Income Added!"
        } else {
            "💸 -$formattedAmount Expense Recorded"
        }
        
        val contentText = "$payer logged $category. " + if (notes.isNotBlank()) "Note: $notes" else ""

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun postEndOfDayNotification(
        context: Context,
        totalIncome: Double,
        totalExpense: Double
    ) {
        val sharedPrefs = context.getSharedPreferences("vault_security_prefs", Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean("notifications_eod_enabled", true)
        if (!isEnabled) return

        createNotificationChannel(context)

        val format = NumberFormat.getCurrencyInstance(Locale.US)
        val formattedIncome = format.format(totalIncome)
        val formattedExpense = format.format(totalExpense)

        val netSavings = totalIncome - totalExpense
        val formattedSavings = format.format(netSavings)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            99999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val iconId = com.example.R.mipmap.ic_launcher
        val contentTitle = "🌅 End-of-Day Nest Summary"
        val contentText = "Earned: +$formattedIncome | Spent: -$formattedExpense"
        
        val bigText = "Today's ledger balance: \n" +
                "📈 Total Earned: $formattedIncome\n" +
                "📉 Total Spent: $formattedExpense\n" +
                "⚖️ Net Change: ${if (netSavings >= 0) "+$formattedSavings" else formattedSavings}\n\n" +
                "FinNest master vault has secured today's entries locally and safely in your family ledger."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(99999, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
