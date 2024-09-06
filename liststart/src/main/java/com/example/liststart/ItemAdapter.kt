package com.example.liststart
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 데이터 클래스 정의 (예시)


class ItemAdapter(private val itemList: List<Item> , private val func: (data: Item) -> Unit) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // ViewHolder 클래스
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val itemRoot = view.rootView

        val titleText: TextView = view.findViewById(R.id.titleText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val profileImage: ImageView = view.findViewById(R.id.profileImage)

    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.titleText.text = item.title
        holder.dateText.text = item.date
        holder.profileImage.setImageResource(item.profileImage) // 예시로 drawable 리소스를 사용함

        holder.itemRoot.setOnClickListener{
            func(item)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount() = itemList.size
}
