package com.example.liststart

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private var itemList: MutableList<Item>,
    private var isVisible: Boolean,
    private val func: (data: Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var originalItemList: MutableList<Item> = itemList.toMutableList() // 원본 리스트

    // ViewHolder 클래스
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val itemRoot = view.rootView

        val titleText: TextView = view.findViewById(R.id.titleText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val checkBox: ImageButton = view.findViewById(R.id.checkBoxImageButton)
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        // 타이틀, 날짜, 프로필 이미지 설정
        holder.titleText.text = item.title
        holder.dateText.text = item.date
        holder.profileImage.setImageResource(item.profileImage)
        holder.checkBox.setImageResource(if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox)

        // 체크박스 visibility 변경
        holder.checkBox.visibility = if (isVisible) View.VISIBLE else View.GONE

        // 아이템 클릭 이벤트 설정
        holder.itemRoot.setOnClickListener {
            if (!isVisible) func(item)
            else {
                item.isChecked = !item.isChecked
                val checkboxImage = if(item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox
                holder.checkBox.setImageResource(checkboxImage)
            }
        }

        holder.checkBox.setOnClickListener {
            if(isVisible) {
                item.isChecked = !item.isChecked
                val checkboxImage = if(item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox
                holder.checkBox.setImageResource(checkboxImage)
            }
        }
    }

    // 아이템 개수 반환
    override fun getItemCount() = itemList.size

    fun updateVisibility(visible: Boolean) {
        this.isVisible = visible
        notifyDataSetChanged() // 데이터 갱신
    }

    // 체크된 아이템 삭제
    fun deleteCheckedItems() {
        originalItemList = originalItemList.filter { !it.isChecked }.toMutableList() // 체크되지 않은 항목만 유지
        itemList = originalItemList
        notifyDataSetChanged() // 리스트 갱신
    }

    fun addItem(data: Item) {
        originalItemList.add(data)
        itemList = originalItemList
        notifyItemInserted(itemList.size - 1)
    }

    // 검색 기능 구현
    fun filterItem(target: String) {
        itemList = if (target.isBlank()) {
            originalItemList.toMutableList() // 검색어가 없을 경우 원본 리스트 복원
        } else {
            originalItemList.filter { it.title.contains(target, ignoreCase = true) }.toMutableList() // 검색 결과
        }
        notifyDataSetChanged()
    }

    // 체크 확인
    fun isAnyChecked(): Boolean {
        return !itemList.none { it.isChecked }
    }

    // 체크된 항목 title 불러오기
    fun getCheckedItemTitle(): String {

        // 체크된 항목들의 타이틀을 리스트로 가져오기
        val checkedItems = itemList.filter { it.isChecked }.map { it.title }
        val size = checkedItems.size

        return if(size > 3) "선택된 사업 ${size}개" else checkedItems.joinToString("\n")
    }

    fun changeIsCheckedToDefault() {
        itemList.forEach { if(it.isChecked) it.isChecked = false }
        notifyDataSetChanged()
    }
 }

