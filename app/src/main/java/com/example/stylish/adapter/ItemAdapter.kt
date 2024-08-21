package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stylish.R
import com.example.stylish.model.Item
import com.facebook.shimmer.ShimmerFrameLayout

class ItemAdapter(
    private val itemList: List<Item>,
    private val isLoading: Boolean
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_SHIMMER = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName : TextView = itemView.findViewById(R.id.itemNameCard)
        val itemPrice : TextView = itemView.findViewById(R.id.itemPrice_incard)
    }
    inner class shimmerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val shimmerFrameLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_item_cloth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM){
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_card_cloth, parent, false)
            ItemViewHolder(itemView)
        }else{
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_shimmer_card_cloth, parent, false)
            shimmerViewHolder(itemView)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position)== VIEW_TYPE_ITEM){
            val currentItem = itemList[position]
            val itemViewHolder = holder as ItemViewHolder
            itemViewHolder.itemName.text = currentItem.title
            itemViewHolder.itemPrice.text = currentItem.price

        }else{
            val shimmerViewHolder = holder as shimmerViewHolder
            shimmerViewHolder.shimmerFrameLayout.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            10// Number of shimmer placeholders to show
        } else {
            itemList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }


}
