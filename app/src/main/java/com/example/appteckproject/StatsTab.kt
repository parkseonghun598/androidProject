package com.example.appteckproject

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

fun Activity.showStatsTab(
    scheduleList: List<MainActivity.Schedule>,
    repository: ScheduleRepository,
    userId: String,
    selectedName: String? = null
) {
    val inflater = LayoutInflater.from(this)
    val statsView = inflater.inflate(R.layout.layout_stats, null, false)

    val spinnerSchedule = statsView.findViewById<Spinner>(R.id.spinnerSchedule)
    val tvAttendanceCount = statsView.findViewById<TextView>(R.id.tvAttendanceCount)
    val tvAbsenceCount = statsView.findViewById<TextView>(R.id.tvAbsenceCount)
    val tvAccumulatedReward = statsView.findViewById<TextView>(R.id.tvAccumulatedReward)
    val calendarGrid = statsView.findViewById<GridLayout>(R.id.calendarGrid)
    val btnCalcStats = statsView.findViewById<Button>(R.id.btnCalcStats)

    val scheduleNames = scheduleList.map { it.name }
    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, scheduleNames)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinnerSchedule.adapter = adapter

    var isSpinnerInitialized = false
    val initialSelectedName = selectedName ?: scheduleNames.firstOrNull()
    if (initialSelectedName != null) {
        val initialIndex = scheduleNames.indexOf(initialSelectedName)
        if (initialIndex >= 0) spinnerSchedule.setSelection(initialIndex)
    }

    // Spinner 선택 이벤트
    spinnerSchedule.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            if (!isSpinnerInitialized) {
                isSpinnerInitialized = true
                return
            }
            val selectedName = scheduleNames[position]
            showStatsTab(scheduleList, repository, userId, selectedName)
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    // 선택된 일정만 통계 집계
    val selectedSchedule = scheduleList.find { it.name == (selectedName ?: scheduleNames.firstOrNull()) }
    if (selectedSchedule == null) {
        // 일정이 없으면 통계 초기화
        tvAttendanceCount.text = "출석\n0"
        tvAbsenceCount.text = "결석\n0"
        tvAccumulatedReward.text = "현재까지의 적립금 : 0원"
        calendarGrid.removeAllViews()
        val tabContent = findViewById<FrameLayout>(R.id.tabContent)
        tabContent.removeAllViews()
        tabContent.addView(statsView)
        return
    }

    val month = repository.thisMonthString()
    val totalDays = 30
    val attendanceDays = mutableSetOf<Int>()
    val absenceDays = mutableSetOf<Int>()

    // 로그인한 사용자의 joinDate 불러오기
    val userDbHelper = UserDBHelper(this)
    val userDb = userDbHelper.readableDatabase
    var joinDate = "1970-01-01"
    val cursor = userDb.rawQuery("SELECT joinDate FROM users WHERE id = ?", arrayOf(userId))
    if (cursor.moveToFirst()) {
        joinDate = cursor.getString(0)
    }
    cursor.close()
    userDb.close()

    val today = repository.todayDateString() // yyyy-MM-dd
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val joinDateObj = dateFormat.parse(joinDate)
    val todayObj = dateFormat.parse(today)

    for (day in 1..totalDays) {
        val dateStr = String.format("%s-%02d", month, day)
        val dateObj = dateFormat.parse(dateStr)
        if (dateObj.before(joinDateObj) || dateObj.after(todayObj)) continue
        if (repository.isAttendanceMarked(selectedSchedule.name, dateStr)) {
            attendanceDays.add(day)
        } else {
            absenceDays.add(day)
        }
    }

    val attendanceCount = attendanceDays.size
    val absenceCount = absenceDays.size

    tvAttendanceCount.text = "출석\n$attendanceCount"
    tvAbsenceCount.text = "결석\n$absenceCount"

    // 누적 적립금 계산 (출석일수 × 1일 예상 적립금)
    val dailyReward = selectedSchedule.reward
    val accumulated = attendanceCount * dailyReward
    tvAccumulatedReward.text = "현재까지의 적립금 : ${accumulated}원"

    // 달력 표시
    calendarGrid.removeAllViews()
    for (i in 1..totalDays) {
        val dateStr = String.format("%s-%02d", month, i)
        val dateObj = dateFormat.parse(dateStr)
        val tv = TextView(this).apply {
            text = i.toString()
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 16)
            if (dateObj.before(joinDateObj) || dateObj.after(todayObj)) {
                setTextColor(Color.parseColor("#CCCCCC")) // 비활성화 색상
            } else if (absenceDays.contains(i)) {
                setBackgroundResource(R.drawable.red_circle)
                setTextColor(Color.WHITE)
            }
        }
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = LinearLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(4, 4, 4, 4) // 각 셀 사이의 간격
        }
        tv.layoutParams = params
        calendarGrid.addView(tv)
    }

    btnCalcStats.setOnClickListener { (this as MainActivity).showExpectedRewardCalcPage() }

    val tabContent = findViewById<FrameLayout>(R.id.tabContent)
    tabContent.removeAllViews()
    tabContent.addView(statsView)
} 