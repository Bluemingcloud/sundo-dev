package com.example.liststart

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.liststart.databinding.CustomDialogBinding
import com.example.liststart.databinding.DeleteDialogBinding

const val TAG = "myLog"

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemList: MutableList<Item>
    private lateinit var itemAdapter: ItemAdapter
    private var isVisible = false
    private lateinit var searchEditText: EditText // 검색 editText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)

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
        itemAdapter = ItemAdapter(itemList, isVisible, { item -> handleClick(item) })

        // RecyclerView에 어댑터 설정
        recyclerView.adapter = itemAdapter

        // 검색 버튼 클릭 리스너
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            val target = findViewById<EditText>(R.id.searchEditText).text.toString()
            itemAdapter.filterItem(target)
        }

        // EditText 초기화
        searchEditText = findViewById(R.id.searchEditText)

        // 검색어 입력시 자동 필터링
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                itemAdapter.filterItem(query) // 검색어에 맞는 아이템 필터링
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        setupUI(rootLayout)

        // 검색 포커스 밖에서 키보드 숨김처리
        searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                // 키보드 숨기기
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        // 검색 버튼 클릭 시 포커스 해제 및 키보드 숨기기
        searchButton.setOnClickListener {
            searchEditText.clearFocus()
            hideKeyboard()
        }

        // 추가 버튼 클릭 리스너
        val addButton = findViewById<ImageButton>(R.id.addButton)
        addButton.setOnClickListener {
            handleAddBtnClick()
        }

        // 삭제 버튼 클릭 시 체크박스가 보이면 삭제, 아니면 체크박스 표시
        val deleteButton = findViewById<ImageButton>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            if (isVisible) {
                handleDeleteBtnClick()
            }
            toggleCheckBoxVisibility()
        }
    }

    // 키보드 숨김 처리
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    // 포커스 처리
    private fun setupUI(view: View) {
        // EditText가 아닌 영역을 클릭하면 키보드를 숨기고 포커스를 해제합니다.
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                searchEditText.clearFocus()
                false
            }
        }

        // 이 뷰의 자식들에도 동일한 동작을 적용합니다.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    private fun toggleCheckBoxVisibility() {
        isVisible = !isVisible
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

    private fun handleAddBtnClick() {
        val customDialog = Dialog(this, R.style.CustomDialogTheme)
        val dialogBinding = CustomDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        dialogResize(this, customDialog, 0.9f)

        dialogBinding.dialogCancel.setOnClickListener {
            customDialog.dismiss()
        }

        dialogBinding.dialogConfirm.setOnClickListener {
            val title = dialogBinding.addEditText.text.toString()

            if (title.isBlank()) {
                dialogBinding.addEditText.error = "Title cannot be empty"
            } else {
                addItem(title, "2024.09.05")
                customDialog.dismiss()
            }
        }

        customDialog.show()
    }

    private fun handleDeleteBtnClick() {

        if(itemAdapter.isAnyChecked()) {
            val deleteDialog = Dialog(this, R.style.CustomDialogTheme)
            val dialogBinding = DeleteDialogBinding.inflate(layoutInflater)
            dialogBinding.deleteItem.text = itemAdapter.getCheckedItemTitle()
            deleteDialog.setContentView(dialogBinding.root)
            dialogResize(this, deleteDialog, 0.9f)

            dialogBinding.dialogCancel.setOnClickListener {
                itemAdapter.changeIsCheckedToDefault()
                deleteDialog.dismiss()
            }

            dialogBinding.dialogConfirm.setOnClickListener {
                itemAdapter.deleteCheckedItems() // 체크된 항목 삭제
                searchEditText.text.clear() // 검색 입력 초기화
                deleteDialog.dismiss()
            }

            deleteDialog.show()
        } else {
            return
        }

    }


    private fun dialogResize(context: Context, dialog: Dialog, width: Float, height: Float = 0f) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            point
        } else {
            windowManager.currentWindowMetrics.bounds.let { Point(it.width(), it.height()) }
        }

        dialog.window?.setLayout(
            (size.x * width).toInt(),
            if (height != 0f) (size.y * height).toInt() else WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun addItem(title: String, date: String) {
        val item = Item(title, date)
        itemAdapter.addItem(item)
    }
}
