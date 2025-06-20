package com.example.appteckproject

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 2초 후에 MainActivity로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            val userId = intent.getStringExtra("user_id")
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.putExtra("user_id", userId)
            startActivity(mainIntent)
            finish()
        }, 2000)
    }
} 