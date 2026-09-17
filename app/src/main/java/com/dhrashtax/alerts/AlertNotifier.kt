package com.dhrashtax.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.dhrashtax.R
import com.dhrashtax.domain.RuleVerdict
import java.util.Locale

class AlertNotifier(private val context: Context) : TextToSpeech.OnInitListener {
    private var ttsReady = false
    private val tts = TextToSpeech(context.applicationContext, this)

    init {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.alert_channel_name), NotificationManager.IMPORTANCE_HIGH),
        )
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) tts.language = Locale.getDefault()
    }

    fun notify(id: Long, verdict: RuleVerdict, voiceEnabled: Boolean) {
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(verdict.threatClass.name.replace('_', ' '))
            .setContentText("${verdict.packageName}: ${verdict.summary}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(verdict.evidence.joinToString("\n")))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id.toInt(), notification)
        if (voiceEnabled && ttsReady) {
            tts.speak(
                "${verdict.threatClass.name.replace('_', ' ')} detected",
                TextToSpeech.QUEUE_ADD,
                null,
                "alert-$id",
            )
        }
    }

    fun close() = tts.shutdown()

    private companion object { const val CHANNEL = "security_alerts" }
}
