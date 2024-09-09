package com.example.liststart
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 데이터 클래스 정의 (예시)

class ItemAdapter(
    private val itemList: List<Item>,
    private var isVisible: Boolean, // 상태를 var로 변경
    private val func: (data: Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

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

        // isCheckedVisible에 따라 체크박스 visibility 변경
        holder.checkBox.visibility = if (isVisible) View.VISIBLE else View.GONE

        // 아이템 클릭 이벤트 설정
        holder.itemRoot.setOnClickListener {
            if (!isVisible) func(item)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount() = itemList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateVisibility(visible: Boolean) {
        this.isVisible = visible
        notifyDataSetChanged() // 데이터 갱신
    }
}
