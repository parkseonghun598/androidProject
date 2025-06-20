package com.example.appteckproject

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

fun Activity.showScheduleTab(
    scheduleList: MutableList<MainActivity.Schedule>,
    repository: ScheduleRepository,
    showScheduleDetail: (MainActivity.Schedule?) -> Unit
) {
    val inflater = LayoutInflater.from(this)
    val scheduleView = inflater.inflate(R.layout.layout_schedule, null, false)

    val dateSelector = scheduleView.findViewById<LinearLayout>(R.id.dateSelector)
    val scheduleListLayout = scheduleView.findViewById<LinearLayout>(R.id.scheduleList)
    scheduleListLayout.removeAllViews()

    // 오늘 기준 -3~+3일 날짜/요일 버튼 동적 생성
    dateSelector.removeAllViews()
    val today = Calendar.getInstance()
    val baseDate = today
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    val dayFormat = SimpleDateFormat("E", Locale.KOREAN) // 요일 한글
    val buttons = mutableListOf<Button>()
    for (i in -3..3) {
        val cal = Calendar.getInstance()
        cal.time = today.time
        cal.add(Calendar.DATE, i)
        val btn = Button(this).apply {
            text = "${dateFormat.format(cal.time)}\n${dayFormat.format(cal.time)}"
            textSize = 14f
            setPadding(0, 0, 0, 0)
            minWidth = 0
            minHeight = 0
            setBackgroundColor(if (cal.get(Calendar.YEAR) == baseDate.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == baseDate.get(Calendar.DAY_OF_YEAR)) Color.parseColor("#7A9B7A") else Color.TRANSPARENT)
            setTextColor(if (cal.get(Calendar.YEAR) == baseDate.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == baseDate.get(Calendar.DAY_OF_YEAR)) Color.WHITE else Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                // 선택된 날짜의 요일에 체크된 일정만 표시
                val selectedCal = cal
                val selectedDayOfWeek = selectedCal.get(Calendar.DAY_OF_WEEK) // 1~7 (일~토)
                val filteredSchedules = scheduleList.filter { it.days.contains(selectedDayOfWeek) }

                // 일정 목록 갱신
                scheduleListLayout.removeAllViews()
                for (schedule in filteredSchedules) {
                    val row = LinearLayout(this@showScheduleTab).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 40, 0, 40)
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, 32)
                        }
                    }

                    // 출석 체크박스 (오늘만 활성화)
                    val isToday = selectedCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && selectedCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                    val checkBox = CheckBox(this@showScheduleTab).apply {
                        isChecked = repository.isAttendanceMarked(schedule.name, dateStr)
                        isEnabled = isToday // 오늘만 체크 가능
                        setOnCheckedChangeListener { _, isChecked ->
                            if (!isToday) return@setOnCheckedChangeListener
                            if (isChecked) {
                                repository.markAttendance(schedule.name, dateStr)
                            } else {
                                repository.unmarkAttendance(schedule.name, dateStr)
                            }
                        }
                    }
                    row.addView(checkBox)

                    val clock = ImageView(this@showScheduleTab).apply {
                        setImageResource(android.R.drawable.ic_lock_idle_alarm)
                        layoutParams = LinearLayout.LayoutParams(100, 100)
                    }

                    val textCol = LinearLayout(this@showScheduleTab).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(32, 0, 0, 0)
                    }

                    val appName = TextView(this@showScheduleTab).apply {
                        text = schedule.name
                        textSize = 22f
                        setTextColor(0xFF000000.toInt())
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    val appTime = TextView(this@showScheduleTab).apply {
                        text = String.format("%02d:%02d", schedule.hour, schedule.minute)
                        textSize = 24f
                        setTextColor(0xFF000000.toInt())
                    }
                    textCol.addView(appName)
                    textCol.addView(appTime)

                    row.addView(clock)
                    row.addView(textCol)

                    // 일정 row 클릭 시 수정 화면 진입
                    row.setOnClickListener {
                        Log.d("AlarmTest", "일정 row 클릭: ${schedule.name}")
                        showScheduleDetail(schedule)
                    }

                    scheduleListLayout.addView(row)
                }

                // 버튼 색상 업데이트
                buttons.forEach { btn ->
                    val btnCal = Calendar.getInstance()
                    btnCal.time = today.time
                    btnCal.add(Calendar.DATE, buttons.indexOf(btn) - 3)
                    btn.setBackgroundColor(if (btnCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) && btnCal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)) Color.parseColor("#7A9B7A") else Color.TRANSPARENT)
                    btn.setTextColor(if (btnCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) && btnCal.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)) Color.WHITE else Color.BLACK)
                }
            }
        }
        dateSelector.addView(btn)
        buttons.add(btn)
    }

    // 초기 로드 시 오늘 날짜의 일정 표시
    val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
    val todaySchedules = scheduleList.filter { it.days.contains(todayDayOfWeek) }
    for (schedule in todaySchedules) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 40, 0, 40)
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }

        // 출석 체크박스 (오늘만 활성화)
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
        val checkBox = CheckBox(this).apply {
            isChecked = repository.isAttendanceMarked(schedule.name, dateStr)
            isEnabled = true // 오늘은 체크 가능
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    repository.markAttendance(schedule.name, dateStr)
                } else {
                    repository.unmarkAttendance(schedule.name, dateStr)
                }
            }
        }
        row.addView(checkBox)

        val clock = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_lock_idle_alarm)
            layoutParams = LinearLayout.LayoutParams(100, 100)
        }

        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 0, 0, 0)
        }

        val appName = TextView(this).apply {
            text = schedule.name
            textSize = 22f
            setTextColor(0xFF000000.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        val appTime = TextView(this).apply {
            text = String.format("%02d:%02d", schedule.hour, schedule.minute)
            textSize = 24f
            setTextColor(0xFF000000.toInt())
        }
        textCol.addView(appName)
        textCol.addView(appTime)

        row.addView(clock)
        row.addView(textCol)

        // 일정 row 클릭 시 수정 화면 진입
        row.setOnClickListener {
            Log.d("AlarmTest", "일정 row 클릭: ${schedule.name}")
            showScheduleDetail(schedule)
        }

        scheduleListLayout.addView(row)
    }

    // 일정 추가 버튼 리스너 연결
    val btnAddSchedule = scheduleView.findViewById<Button>(R.id.btnAddSchedule)
    btnAddSchedule?.setOnClickListener {
        Log.d("AlarmTest", "일정 추가 버튼 클릭")
        showScheduleDetail(null)
    }

    val tabContent = findViewById<FrameLayout>(R.id.tabContent)
    tabContent.removeAllViews()
    tabContent.addView(scheduleView)
}

fun Activity.showScheduleDetail(
    schedule: MainActivity.Schedule?,
    scheduleList: MutableList<MainActivity.Schedule>,
    repository: ScheduleRepository,
    showScheduleTab: () -> Unit
) {
    Log.d("AlarmTest", "showScheduleDetail 함수 진입")
    val inflater = LayoutInflater.from(this)
    val detailView = inflater.inflate(R.layout.layout_schedule_detail, null, false)

    val btnRegister = detailView.findViewById<Button>(R.id.btnRegisterSchedule)
    if (btnRegister == null) {
        Log.e("AlarmTest", "btnRegisterSchedule 버튼을 찾을 수 없음!")
    }
    val etAppName = detailView.findViewById<EditText>(R.id.etAppName)
    val etReward = detailView.findViewById<EditText>(R.id.etReward)
    val etRewardStreak = detailView.findViewById<EditText>(R.id.etRewardStreak)
    val timePicker = detailView.findViewById<TimePicker>(R.id.timePicker)
    val daySelector = detailView.findViewById<LinearLayout>(R.id.daySelector)
    val switchAlarm = detailView.findViewById<Switch>(R.id.switchAlarm)
    val seekBarVolume = detailView.findViewById<SeekBar>(R.id.seekBarVolume)
    val btnDelete = detailView.findViewById<Button>(R.id.btnDeleteSchedule)

    // 요일 선택 버튼 리스트 및 Calendar 요일 매핑
    val dayButtons = listOf(
        detailView.findViewById<Button>(R.id.btnMon),
        detailView.findViewById<Button>(R.id.btnTue),
        detailView.findViewById<Button>(R.id.btnWed),
        detailView.findViewById<Button>(R.id.btnThu),
        detailView.findViewById<Button>(R.id.btnFri),
        detailView.findViewById<Button>(R.id.btnSat),
        detailView.findViewById<Button>(R.id.btnSun)
    )
    val dayNums = listOf(2, 3, 4, 5, 6, 7, 1) // Calendar.MONDAY~SUNDAY
    val selectedDays = mutableSetOf<Int>()

    // 버튼 클릭 시 선택/해제 및 색상 토글
    dayButtons.forEachIndexed { idx, btn ->
        btn.setOnClickListener {
            val dayNum = dayNums[idx]
            if (selectedDays.contains(dayNum)) {
                selectedDays.remove(dayNum)
                btn.setBackgroundColor(Color.TRANSPARENT)
                btn.setTextColor(Color.BLACK)
            } else {
                selectedDays.add(dayNum)
                btn.setBackgroundColor(Color.parseColor("#7A9B7A"))
                btn.setTextColor(Color.WHITE)
            }
        }
    }

    // 기존 일정 정보로 입력란 채우기 (수정 모드)
    if (schedule != null) {
        etAppName.setText(schedule.name)
        etAppName.isEnabled = false // 이름은 수정 불가(고유키)
        etReward.setText(schedule.reward.toString())
        etRewardStreak.setText(schedule.rewardStreak.toString())
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = schedule.hour
            timePicker.minute = schedule.minute
        } else {
            timePicker.currentHour = schedule.hour
            timePicker.currentMinute = schedule.minute
        }
        switchAlarm.isChecked = schedule.alarmOn
        seekBarVolume.progress = schedule.volume
        // 요일 선택 반영
        for (i in dayButtons.indices) {
            val dayNum = dayNums[i]
            if (schedule.days.contains(dayNum)) {
                selectedDays.add(dayNum)
                dayButtons[i].setBackgroundColor(Color.parseColor("#7A9B7A"))
                dayButtons[i].setTextColor(Color.WHITE)
            }
        }
        btnRegister.text = "일정 수정하기"
        btnDelete.visibility = View.VISIBLE
    }

    btnRegister.setOnClickListener {
        Log.d("AlarmTest", "btnRegister 클릭됨")
        val name = etAppName.text.toString().trim()
        val reward = etReward.text.toString().toIntOrNull() ?: 0
        val rewardStreak = etRewardStreak.text.toString().toIntOrNull() ?: 0
        if (name.isEmpty()) {
            Toast.makeText(this, "앱 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        val hour = if (android.os.Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
        val minute = if (android.os.Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
        val alarmOn = switchAlarm.isChecked
        val volume = seekBarVolume.progress

        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "요일을 선택하세요.", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        val newSchedule = MainActivity.Schedule(name, hour, minute, selectedDays.toList(), reward, rewardStreak, alarmOn, volume)
        if (schedule == null) {
            // 신규 등록
            scheduleList.add(newSchedule)
            repository.insert(newSchedule)
            if (alarmOn) {
                Log.d("AlarmTest", "btnRegister: setAlarm 호출(신규)")
                setAlarm(newSchedule)
            }
            Toast.makeText(this, "일정이 등록되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 수정 모드: DB update
            cancelAlarm(schedule)
            repository.update(newSchedule)
            if (alarmOn) {
                Log.d("AlarmTest", "btnRegister: setAlarm 호출(수정)")
                setAlarm(newSchedule)
            }
            val idx = scheduleList.indexOfFirst { it.name == newSchedule.name }
            if (idx >= 0) scheduleList[idx] = newSchedule
            Toast.makeText(this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show()
        }
        showScheduleTab()
    }

    btnDelete.setOnClickListener {
        if (schedule != null) {
            cancelAlarm(schedule)
            repository.delete(schedule.name)
            scheduleList.removeAll { it.name == schedule.name }
            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            showScheduleTab()
        }
    }

    val tabContent = findViewById<FrameLayout>(R.id.tabContent)
    tabContent.removeAllViews()
    tabContent.addView(detailView)
} 