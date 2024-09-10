package com.example.liststart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private var isVisible: Boolean,
    private val func: (data: Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private lateinit var app: MyApplication
    private var filteredList: ArrayList<Item> = arrayListOf()

    fun setApplicationContext(context: Context) {
        app = context.applicationContext as MyApplication
        filteredList = app.getItemList() // 초기에는 전체 리스트를 필터링 리스트에 복사
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemRoot = view.rootView
        val titleText: TextView = view.findViewById(R.id.titleText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val checkBox: ImageButton = view.findViewById(R.id.checkBoxImageButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = filteredList[position]

        holder.titleText.text = item.title
        holder.dateText.text = item.date
        holder.profileImage.setImageResource(item.profileImage)
        holder.checkBox.setImageResource(if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox)

        holder.checkBox.visibility = if (isVisible) View.VISIBLE else View.GONE

        holder.itemRoot.setOnClickListener {
            if (!isVisible) {
                func(item)
            } else {
                item.isChecked = !item.isChecked
                holder.checkBox.setImageResource(if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox)
            }
        }

        holder.checkBox.setOnClickListener {
            if (isVisible) {
                item.isChecked = !item.isChecked
                holder.checkBox.setImageResource(if (item.isChecked) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox)
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    // 필터링된 리스트 설정
    fun setFilteredList(newList: ArrayList<Item>) {
        filteredList = newList
        notifyDataSetChanged()
    }

    fun updateVisibility(visible: Boolean) {
        this.isVisible = visible
        notifyDataSetChanged() // UI 갱신
    }

    fun deleteCheckedItems() {
        app.deleteCheckedItems()
        filteredList = app.getItemList() // 원본 리스트에서 필터링된 데이터 다시 설정
        notifyDataSetChanged()
    }

    fun addItem(data: Item) {
        app.addItem(data)
        filteredList = app.getItemList()
        notifyItemInserted(filteredList.size - 1)
    }

    fun isAnyChecked(): Boolean {
        return filteredList.any { it.isChecked }
    }

    fun getCheckedItemTitle(): String {
        val checkedItems = filteredList.filter { it.isChecked }.map { it.title }
        return if (checkedItems.size > 3) "선택된 사업 ${checkedItems.size}개" else checkedItems.joinToString("\n")
    }

    fun changeIsCheckedToDefault() {
        app.resetCheckStatus()
        notifyDataSetChanged()
    }
}
