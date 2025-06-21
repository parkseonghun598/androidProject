package com.example.appteckproject

import android.app.Activity
import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.*
import java.util.*

fun Activity.showRecommendTab(apps: List<String>) {
    val inflater = LayoutInflater.from(this)
    val recommendView = inflater.inflate(R.layout.layout_recommend, null, false)

    val recommendList = recommendView.findViewById<LinearLayout>(R.id.recyclerRecommend)


    // 앱 리스트 랜덤 셔플 후 3개 선택
    val shuffled = apps.toMutableList()
    Collections.shuffle(shuffled)
    val selected = shuffled.subList(0, 3).toMutableList()
    val remain = shuffled.subList(3, shuffled.size).toMutableList()

    // 체크박스와 텍스트뷰를 동적으로 생성 및 교체 기능 구현
    for (i in 0..2) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 32, 0, 32)
        }

        val checkBox = CheckBox(this).apply {
            buttonDrawable = getDrawable(android.R.drawable.btn_radio)
            isFocusable = false
        }

        val tv = TextView(this).apply {
            text = selected[i]
            textSize = 22f
            setTextColor(0xFF000000.toInt())
            setPadding(32, 0, 0, 0)
            typeface = Typeface.DEFAULT_BOLD
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && remain.isNotEmpty()) {
                // 다른 추천 어플로 교체
                val newApp = remain.removeAt(0)
                tv.text = newApp
                checkBox.isChecked = false // 다시 체크 해제
            } else if (isChecked) {
                // 더 이상 교체할 앱이 없으면 체크 해제
                checkBox.isChecked = false
            }
        }

        item.addView(checkBox)
        item.addView(tv)
        recommendList.addView(item)
    }

    // 탭 컨텐츠 영역에 추천 뷰 표시
    val tabContent = findViewById<FrameLayout>(R.id.tabContent)
    tabContent.removeAllViews()
    tabContent.addView(recommendView)
} 