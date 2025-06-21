package com.example.appteckproject

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import java.util.*

fun Activity.setAlarm(schedule: MainActivity.Schedule) {
    Log.d("AlarmTest", "setAlarm 함수 진입: ${schedule.name}")
    // 방해 금지 모드 확인
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Log.d("AlarmTest", "방해 금지 모드 접근 권한 있음")
        } else {
            Log.d("AlarmTest", "방해 금지 모드 접근 권한 없음")
            Toast.makeText(this, "알람이 제대로 작동하려면 방해 금지 모드 설정이 필요합니다.", Toast.LENGTH_LONG).show()
            // 방해 금지 모드 설정 화면으로 이동
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
            return
        }
    }


    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, AlarmReceiver::class.java).apply {
        putExtra("schedule_name", schedule.name)
        putExtra("schedule_hour", schedule.hour)
        putExtra("schedule_minute", schedule.minute)
        putExtra("volume", schedule.volume)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        schedule.name.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 현재 요일이 설정된 요일 중 하나인지 확인
    val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    if (!schedule.days.contains(currentDayOfWeek)) {
        Log.d("AlarmTest", "현재 요일($currentDayOfWeek)이 설정된 요일(${schedule.days})에 포함되지 않음")
        return
    }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, schedule.hour)
        set(Calendar.MINUTE, schedule.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        
        // 현재 시간이 지났으면 다음 날로 설정
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        Log.d("AlarmTest", "알람 설정됨: ${schedule.name}, 시간: ${schedule.hour}:${schedule.minute}")
        Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("AlarmTest", "알람 설정 실패: ${e.message}")
        Toast.makeText(this, "알람 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
    }
}

fun Activity.cancelAlarm(schedule: MainActivity.Schedule) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(this, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        this,
        schedule.name.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    try {
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmTest", "알람 취소됨: ${schedule.name}")
    } catch (e: Exception) {
        Log.e("AlarmTest", "알람 취소 실패: ${e.message}")
    }
} 