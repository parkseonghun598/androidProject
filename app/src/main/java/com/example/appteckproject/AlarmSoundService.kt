package com.example.appteckproject

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log

class AlarmSoundService : Service() {
    companion object {
        const val ACTION_STOP = "com.example.appteckproject.action.STOP_ALARM"
        var isPlay = false
    }
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_sound", "Alarm Sound", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            val notification: Notification = Notification.Builder(this, "alarm_sound")
                .setContentTitle("알람")
                .setContentText("알람이 울리고 있습니다.")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // 이미 음악이 재생 중이면 새로 재생하지 않음
        if (isPlay) return START_NOT_STICKY

        val volume = intent?.getIntExtra("volume", 100) ?: 100
        val soundUri = android.net.Uri.parse("android.resource://${packageName}/raw/antifreeze")
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@AlarmSoundService, soundUri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            setOnPreparedListener {
                val vol = volume / 100.0f
                it.setVolume(vol, vol)
                it.start()
                isPlay = true
            }
            setOnCompletionListener {
                isPlay = false
                stopSelf()
            }
            setOnErrorListener { mp, what, extra ->
                isPlay = false
                stopSelf()
                false
            }
            prepareAsync()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        isPlay = false
        mediaPlayer?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
