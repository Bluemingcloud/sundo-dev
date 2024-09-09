package com.example.liststart

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.liststart.databinding.CustomDialogBinding

const val TAG = "myLog"

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: MutableList<Item>
    private lateinit var itemAdapter: ItemAdapter
    private var isVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 예시 데이터 생성 및 어댑터 설정
        val exampleList = mutableListOf(
            Item("가산 풍력디지털 단지", "2024.09.05", R.drawable.profile, 37.479180, 126.874852),
            Item("송파구 법조타운", "2024.09.01", R.drawable.profile, 37.483817, 127.112121),
            Item("영등포 스마트 도시", "2024.08.30", R.drawable.profile, 37.529931, 126.887700),
            Item("선도 디지털 단지", "2024.08.30", R.drawable.profile, 37.480417, 126.874323),
            Item("제주 탐라 풍력 단지", "2024.08.30", R.drawable.profile, 33.499911, 126.449403),
            Item("영흥 풍력 단지", "2024.08.30", R.drawable.profile, 37.239810, 126.446187)
        )

        itemList = exampleList

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // itemAdapter 초기화
        itemAdapter = ItemAdapter(itemList, isVisible) { item -> handleClick(item) }

        // RecyclerView에 어댑터 설정
        recyclerView.adapter = itemAdapter

        // 검색 버튼 클릭 리스너
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            val intent = Intent(this, GisActivity::class.java)
            startActivity(intent)
        }

        // 추가 버튼 클릭 리스너
        val addButton = findViewById<ImageButton>(R.id.addButton)
        addButton.setOnClickListener {
            handleAddBtnClick()
        }

        // 버튼 클릭 시 체크박스 보이기
        val deleteButton = findViewById<ImageButton>(R.id.deleteButton) // 삭제버튼 버튼
        deleteButton.setOnClickListener {
            toggleCheckBoxVisibility()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleCheckBoxVisibility() {
        isVisible = !isVisible
        // 쓰레기통 이미지 버튼을 다른 모양으로 변경
        val deleteButton = findViewById<ImageButton>(R.id.deleteButton)
        if (isVisible) {
            deleteButton.setImageResource(R.drawable.ic_check) // 새로운 이미지로 변경
        } else {
            deleteButton.setImageResource(R.drawable.ic_trash_fill) // 원래 이미지로 복원
        }

        // 리사이클러뷰 갱신
        itemAdapter.updateVisibility(isVisible) // updateVisibility 함수 호출하여 갱신
    }

    private fun handleClick(data: Item) {
        Log.d(TAG, ": 실행됨 ${data.title}")
        val intent = Intent(this, GisActivity::class.java)
        val bundle = Bundle()
        bundle.putString("title", data.title)
        bundle.putDouble("lat", data.lat)
        bundle.putDouble("long", data.long)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private fun handleClickDeleteCheckbox() {

    }

    private fun handleAddBtnClick() {
        val customDialog = Dialog(this, R.style.CustomDialogTheme) // activity의 컨텍스트
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater) //커스텀 다이어로그 뷰
        customDialog.setContentView(dialogBinding.root) //바인딩
        dialogResize(this, customDialog, 0.9f)

        //customDialog.setCanceledOnTouchOutside(false) // 알림바깥을 누르더라도 꺼지지 않음
        //customDialog.setCancelable(false) // 다이어로그 상태에서 뒤로가기를 막음

        //다이얼로그 view안에서 이벤트
        dialogBinding.dialogCancel.setOnClickListener { // 취소하기
            customDialog.dismiss()
        }

        dialogBinding.dialogConfirm.setOnClickListener { // 추가하기
            val title = dialogBinding.addEditText.text.toString()

            if (title.isBlank()) {
                dialogBinding.addEditText.error = "Title cannot be empty"
            } else {
                addItem(title, "2024.09.05")
                customDialog.dismiss()
            }
        }

        customDialog.show() //보이기
    }

    private fun dialogResize(context: Context, dialog: Dialog, width: Float, height: Float = 0f){
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT < 30){

            val display = windowManager.defaultDisplay

            val size = Point()
            display.getSize(size)
            val window = dialog.window

            val x = (size.x * width).toInt()
            val y = if(height != 0f) (size.y * height).toInt() else WindowManager.LayoutParams.WRAP_CONTENT
            window?.setLayout(x, y)

        }else{
            val rect = windowManager.currentWindowMetrics.bounds

            val window = dialog.window
            val x = (rect.width() * width).toInt()
            val y = if(height != 0f) (rect.height() * height).toInt() else WindowManager.LayoutParams.WRAP_CONTENT
            window?.setLayout(x, y)
        }
    }

    private fun addItem(title: String, date: String) {
        val item = Item(title, date)
        itemList.add(item)
        itemAdapter.notifyItemInserted(itemList.size - 1) // 마지막에 아이템 추가
    }
}
