package com.example.appteckproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "onReceive 호출됨")
        val serviceIntent = Intent(context, AlarmSoundService::class.java)
        serviceIntent.putExtra("volume", intent.getIntExtra("volume", 100))
        context.startForegroundService(serviceIntent)

        // 알람 제목 전달
        val scheduleName = intent.getStringExtra("schedule_name") ?: "일정"
        val activityIntent = Intent(context, AlarmActivity::class.java)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activityIntent.putExtra("schedule_name", scheduleName)
        context.startActivity(activityIntent)
    }
} 