// MainActivity.kt
package com.example.liststart
import ItemAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 리사이클러뷰 찾기
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // 레이아웃 매니저 설정 (리니어 레이아웃 사용)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 예시 데이터 생성
        val exampleList = listOf(
            Item("가산 풍력디지털 단지", "2024.09.05", R.drawable.profile),
            Item("송파구 법조타운", "2024.09.01", R.drawable.profile),
            Item("영등포 스마트 도시", "2024.08.30", R.drawable.profile),
            Item("선도 디지털 단지", "2024.08.30", R.drawable.profile),
            Item("제주 탐라 풍력 단지", "2024.08.30", R.drawable.profile),
            Item("영흥 풍력 단지", "2024.08.30", R.drawable.profile)

        )

        // 어댑터 설정
        recyclerView.adapter = ItemAdapter(exampleList)

        val a = findViewById<ImageButton>(R.id.searchButton)
        a.setOnClickListener{
            val intent = Intent(this, GisActivity::class.java)
            startActivity(
                intent
            )
        }

    }
}
