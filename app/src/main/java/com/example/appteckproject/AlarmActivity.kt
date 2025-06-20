package com.example.appteckproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        val scheduleName = intent.getStringExtra("schedule_name") ?: "일정"
        findViewById<TextView>(R.id.tvAlarmMessage).text = "\"$scheduleName\" 출석체크 해야해요!"

        findViewById<Button>(R.id.btnStopAlarm).setOnClickListener {
            val stopIntent = Intent(this, AlarmSoundService::class.java)
            stopIntent.action = AlarmSoundService.ACTION_STOP
            startService(stopIntent)
            finish()
        }
    }
} 