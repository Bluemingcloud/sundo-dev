package com.example.myapplication

import android.graphics.Paint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var todolist: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var todoEidt:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        //ArrayList 초기화
        todolist = ArrayList()

        //ArrayAdapter 초기화(context, layout,list)
        adapter = ArrayAdapter(this, R.layout.list_item, todolist)

//        UI객체 생성

        val listView: ListView = findViewById(R.id.list_view)
        val addBtn: Button = findViewById(R.id.add_btn)
        todoEidt = findViewById(R.id.todo_edit)

        //Adapter 적용
        listView.adapter = adapter
        //버튼 이벤트
        addBtn.setOnClickListener {
            addItem()
        }

        //리스트 아이템 클릭 이벤트
        listView.setOnItemClickListener { adapterView, view, i, l ->
            val textView : TextView = view as TextView

            //취소선 넣기
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

    } //onCreate
    private fun addItem(){

        //입력값 변수에 담기
        val  todo : String = todoEidt.text.toString()

        //값이 비워있지 않다면
        if(todo.isNotEmpty()){
            todolist.add(todo)

            //적용
            adapter.notifyDataSetChanged()

            todoEidt.text.clear()
        }else{
            Toast.makeText(this,"할 일을 적어주세요",Toast.LENGTH_SHORT).show()
        }

    }
}