package com.example.appteckproject

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import java.util.*
import android.database.sqlite.SQLiteDatabase
import java.text.SimpleDateFormat
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.appteckproject.showScheduleTab
import com.example.appteckproject.showScheduleDetail
import com.example.appteckproject.showStatsTab
import com.example.appteckproject.showRecommendTab
import com.example.appteckproject.setAlarm
import com.example.appteckproject.cancelAlarm

class MainActivity : Activity() {
    private val APPS = listOf(
        "토스", "국민은행", "캐시워크", "패널 나우", "서베이 링크", "캐시멜로우", "지니어스", "머니워크", "체리포인트",
        "라이프 플래닛", "퍼니지", "야핏 무브", "닥터 다이어리", "머니닷", "틱톡 라이트", "슬립 머니"
    )

    private lateinit var tvUserName: TextView
    private lateinit var tvUserInfo: TextView
    private lateinit var repository: ScheduleRepository
    private val scheduleList = mutableListOf<Schedule>()
    private var isSpinnerInitialized = false

    data class Schedule(
        val name: String,
        val hour: Int,
        val minute: Int,
        val days: List<Int>,
        val reward: Int,
        val rewardStreak: Int,
        val alarmOn: Boolean,
        val volume: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = ScheduleRepository(this)
        scheduleList.clear()
        scheduleList.addAll(repository.getAll())

        val btnTabSchedule = findViewById<Button>(R.id.btnTabSchedule)
        val btnTabRecommend = findViewById<Button>(R.id.btnTabRecommend)
        val btnTabStats = findViewById<Button>(R.id.btnTabStats)
        val underlineSchedule = findViewById<View>(R.id.underlineSchedule)
        val underlineRecommend = findViewById<View>(R.id.underlineRecommend)
        val underlineStats = findViewById<View>(R.id.underlineStats)

        fun selectTab(tab: String) {
            underlineSchedule.visibility = if (tab == "schedule") View.VISIBLE else View.GONE
            underlineRecommend.visibility = if (tab == "recommend") View.VISIBLE else View.GONE
            underlineStats.visibility = if (tab == "stats") View.VISIBLE else View.GONE
        }

        // 기본 탭은 추천
        showRecommendTab(APPS)
        selectTab("recommend")

        btnTabRecommend.setOnClickListener {
            showRecommendTab(APPS)
            selectTab("recommend")
        }
        btnTabSchedule.setOnClickListener {
            showScheduleTab(
                scheduleList,
                repository
            ) { schedule ->
                showScheduleDetail(
                    schedule,
                    scheduleList,
                    repository
                ) {
                    showScheduleTab(scheduleList, repository) { schedule ->
                        showScheduleDetail(schedule, scheduleList, repository) {
                            showScheduleTab(scheduleList, repository) { /* no-op */ }
                        }
                    }
                }
            }
            selectTab("schedule")
        }
        btnTabStats.setOnClickListener {
            val userId = intent.getStringExtra("user_id") ?: ""
            showStatsTab(scheduleList, repository, userId)
            selectTab("stats")
        }
    }

    fun showExpectedRewardCalcPage() {
        val inflater = LayoutInflater.from(this)
        val resultView = inflater.inflate(R.layout.layout_expected_reward_calc, null, false)

        val etBonusDays = resultView.findViewById<EditText>(R.id.etBonusDays)
        val etRewardBase = resultView.findViewById<EditText>(R.id.etRewardBase)
        val bonusRewardContainer = resultView.findViewById<LinearLayout>(R.id.bonusRewardContainer)
        val btnGenBonusInputs = resultView.findViewById<Button>(R.id.btnGenBonusInputs)
        val btnCalc = resultView.findViewById<Button>(R.id.btnCalc)
        val tvTotal = resultView.findViewById<TextView>(R.id.tvTotal)
        val tvAvg = resultView.findViewById<TextView>(R.id.tvAvg)

        btnGenBonusInputs.setOnClickListener {
            bonusRewardContainer.removeAllViews()
            val bonusDays = etBonusDays.text.toString().split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
            for (day in bonusDays) {
                val et = EditText(this).apply {
                    hint = "${day}일차 예상 적립금"
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    id = View.generateViewId()
                    tag = "bonus_$day"
                }
                bonusRewardContainer.addView(et)
            }
        }

        btnCalc.setOnClickListener {
            val base = etRewardBase.text.toString().toIntOrNull() ?: 0
            val bonusDays = etBonusDays.text.toString().split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
            val bonusMap = mutableMapOf<Int, Int>()
            for (i in 0 until bonusRewardContainer.childCount) {
                val et = bonusRewardContainer.getChildAt(i) as? EditText ?: continue
                val day = et.hint.toString().replace("일차 예상 적립금", "").trim().toIntOrNull() ?: continue
                val value = et.text.toString().toIntOrNull() ?: base
                bonusMap[day] = value
            }

            var total = 0
            for (day in 1..30) {
                total += bonusMap[day] ?: base
            }
            val avg = total / 30.0

            tvTotal.text = "총 적립금: ${total}원"
            tvAvg.text = "1일 평균 적립금: ${"%.1f".format(avg)}원"
        }

        val tabContent = findViewById<FrameLayout>(R.id.tabContent)
        tabContent.removeAllViews()
        tabContent.addView(resultView)
    }

    // 요일 숫자를 한글로 변환
    private fun dayIntToKor(day: Int): String = when(day) {
        Calendar.SUNDAY -> "일"
        Calendar.MONDAY -> "월"
        Calendar.TUESDAY -> "화"
        Calendar.WEDNESDAY -> "수"
        Calendar.THURSDAY -> "목"
        Calendar.FRIDAY -> "금"
        Calendar.SATURDAY -> "토"
        else -> ""
    }

    // 추천 탭에 랜덤 5개 앱을 가운데 정렬로 출력
    fun showRecommendTab(apps: List<String>) {
        val inflater = LayoutInflater.from(this)
        val recommendView = inflater.inflate(R.layout.activity_recommend, null, false)
        val recyclerView = recommendView.findViewById<LinearLayout>(R.id.recyclerRecommend)
        recyclerView.removeAllViews()
    
        // 랜덤 아이콘 리스트 (안드로이드 기본 제공 아이콘)
        val defaultIcons = listOf(
            android.R.drawable.ic_dialog_info,
            android.R.drawable.ic_menu_view,
            android.R.drawable.ic_menu_today,
            android.R.drawable.ic_menu_myplaces,
            android.R.drawable.ic_menu_agenda,
            android.R.drawable.ic_lock_idle_alarm
        )
    
        val randomApps = apps.shuffled().take(5)
        for ((index, app) in randomApps.withIndex()) {
            val itemView = inflater.inflate(R.layout.item_recommend_app, recyclerView, false)
            val tvAppName = itemView.findViewById<TextView>(R.id.appName)
            val ivAppIcon = itemView.findViewById<ImageView>(R.id.appIcon)
    
            tvAppName.text = app
    
            // 기본 아이콘을 순서대로 혹은 랜덤하게 적용
            val iconRes = defaultIcons[index % defaultIcons.size]
            ivAppIcon.setImageResource(iconRes)
    
            recyclerView.addView(itemView)
        }
    
        val tabContent = findViewById<FrameLayout>(R.id.tabContent)
        tabContent.removeAllViews()
        tabContent.addView(recommendView)
    }
    
}