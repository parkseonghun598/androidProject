package com.example.appteckproject

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDBHelper(context: Context) : SQLiteOpenHelper(context, "ScheduleDB.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                hour INTEGER,
                minute INTEGER,
                days TEXT,
                reward INTEGER,
                rewardStreak INTEGER,
                alarmOn INTEGER,
                volume INTEGER
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE attendance (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                scheduleName TEXT,
                date TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS schedules")
        db.execSQL("DROP TABLE IF EXISTS attendance")
        onCreate(db)
    }
} 