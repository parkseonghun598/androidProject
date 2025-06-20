package com.example.appteckproject

import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : Activity() {
    private lateinit var dbHelper: UserDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = UserDBHelper(this)
        val etId = findViewById<EditText>(R.id.etId)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPasswordConfirm = findViewById<EditText>(R.id.etPasswordConfirm)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLoginGo = findViewById<TextView>(R.id.btnLoginGo)

        btnRegister.setOnClickListener {
            val id = etId.text.toString()
            val password = etPassword.text.toString()
            val passwordConfirm = etPasswordConfirm.text.toString()

            if (id.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("id", id)
                put("password", password)
                val joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                put("joinDate", joinDate)
            }

            try {
                db.insertOrThrow("users", null, values)
                Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
            } finally {
                db.close()
            }
        }

        btnLoginGo.setOnClickListener {
            finish()
        }
    }
} 