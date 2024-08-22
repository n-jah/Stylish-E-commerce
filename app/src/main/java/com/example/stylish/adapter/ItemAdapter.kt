package com.example.stylish.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.example.stylish.R
import com.example.stylish.model.Item
import com.facebook.shimmer.ShimmerFrameLayout
import com.bumptech.glide.request.RequestOptions

class ItemAdapter(
    private val itemList: List<Item>,
    private val isLoading: Boolean ? = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_SHIMMER = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.product_img_card)  // Change TextView to ImageView
        val itemName: TextView = itemView.findViewById(R.id.itemNameCard)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice_incard)

        fun bind(itemModel: Item, context: Context) {
            itemName.text = itemModel.title
            itemPrice.text = itemModel.price

            val requestOptions = RequestOptions().transforms(CenterInside())

            Glide.with(context)
                .load(itemModel.imageUrl)
                .apply(requestOptions)
                .into(itemImage)
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerFrameLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_item_cloth)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_card_cloth, parent, false)
            ItemViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_shimmer_card_cloth, parent, false)
            ShimmerViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            val itemViewHolder = holder as ItemViewHolder
            val currentItem = itemList[position]
            itemViewHolder.bind(currentItem, holder.itemView.context)
        } else {
            val shimmerViewHolder = holder as ShimmerViewHolder
            shimmerViewHolder.shimmerFrameLayout.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            10 // Number of shimmer placeholders to show
        } else {
            itemList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }
}
