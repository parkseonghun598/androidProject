package com.example.appteckproject

import android.content.Context
import android.database.Cursor
import java.text.SimpleDateFormat
import java.util.*

class ScheduleRepository(context: Context) {
    private val dbHelper = ScheduleDBHelper(context)

    fun insert(schedule: MainActivity.Schedule) {
        val db = dbHelper.writableDatabase
        val daysStr = schedule.days.joinToString(",")
        db.execSQL(
            "INSERT INTO schedules (name, hour, minute, days, reward, rewardStreak, alarmOn, volume) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            arrayOf(
                schedule.name,
                schedule.hour,
                schedule.minute,
                daysStr,
                schedule.reward,
                schedule.rewardStreak,
                if (schedule.alarmOn) 1 else 0,
                schedule.volume
            )
        )
        db.close()
    }

    fun getAll(): List<MainActivity.Schedule> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM schedules", null)
        val list = mutableListOf<MainActivity.Schedule>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"))
            val minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"))
            val days = cursor.getString(cursor.getColumnIndexOrThrow("days")).split(",").map { it.toInt() }
            val reward = cursor.getInt(cursor.getColumnIndexOrThrow("reward"))
            val rewardStreak = cursor.getInt(cursor.getColumnIndexOrThrow("rewardStreak"))
            val alarmOn = cursor.getInt(cursor.getColumnIndexOrThrow("alarmOn")) == 1
            val volume = cursor.getInt(cursor.getColumnIndexOrThrow("volume"))
            list.add(MainActivity.Schedule(name, hour, minute, days, reward, rewardStreak, alarmOn, volume))
        }
        cursor.close()
        db.close()
        return list
    }

    fun markAttendance(scheduleName: String, date: String) {
        val db = dbHelper.writableDatabase
        db.execSQL(
            "INSERT INTO attendance (scheduleName, date) VALUES (?, ?)",
            arrayOf(scheduleName, date)
        )
        db.close()
    }

    fun isAttendanceMarked(scheduleName: String, date: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM attendance WHERE scheduleName = ? AND date = ?",
            arrayOf(scheduleName, date)
        )
        var marked = false
        if (cursor.moveToFirst()) marked = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return marked
    }

    fun getAttendanceCount(scheduleName: String, month: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM attendance WHERE scheduleName = ? AND date LIKE ?",
            arrayOf(scheduleName, "$month%")
        )
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun todayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    fun thisMonthString(): String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    fun unmarkAttendance(scheduleName: String, date: String) {
        val db = dbHelper.writableDatabase
        db.delete("attendance", "scheduleName = ? AND date = ?", arrayOf(scheduleName, date))
        db.close()
    }

    fun update(schedule: MainActivity.Schedule) {
        val db = dbHelper.writableDatabase
        val daysStr = schedule.days.joinToString(",")
        db.execSQL(
            "UPDATE schedules SET hour = ?, minute = ?, days = ?, reward = ?, rewardStreak = ?, alarmOn = ?, volume = ? WHERE name = ?",
            arrayOf(schedule.hour, schedule.minute, daysStr, schedule.reward, schedule.rewardStreak, if (schedule.alarmOn) 1 else 0, schedule.volume, schedule.name)
        )
        db.close()
    }

    fun delete(scheduleName: String) {
        val db = dbHelper.writableDatabase
        db.delete("schedules", "name = ?", arrayOf(scheduleName))
        db.delete("attendance", "scheduleName = ?", arrayOf(scheduleName))
        db.close()
    }
} 