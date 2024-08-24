package com.example.stylish.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.model.Item
import com.example.stylish.ui.home.activity.ItemActivity
import com.facebook.shimmer.ShimmerFrameLayout

@Suppress("DEPRECATION")
class ItemAdapter(
    private val itemList: List<Item> = emptyList(),
    private val isLoading: Boolean = true // Default value to avoid nulls
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_SHIMMER = 1
    private val shimmerItemCount = 10 // Configurable number of shimmer placeholders

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.product_img_card)
        val itemName: TextView = itemView.findViewById(R.id.itemNameCard)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice_incard)

        fun bind(itemModel: Item, context: Context) {
            itemName.text = itemModel.description
            itemPrice.text = itemModel.price

            val requestOptions = RequestOptions().transforms(CenterInside())
            if (itemModel.imgURl.isNotEmpty()) {
                Glide.with(context)
                    .load(itemModel.imgURl)
                    .apply(requestOptions)
                    .into(itemImage)
            } else {
                // Handle the case when imageUrl is empty or null
                itemImage.setImageResource(R.drawable.placeholder) // Set a placeholder image
                Log.e("ItemAdapter", "Image URL is empty for item: ${itemModel.description}")
            }



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


//            itemViewHolder.itemView.setOnClickListener {
//                // Handle item click here
//                val intent = Intent(holder.itemView.context, ItemActivity::class.java)
//                intent.putExtra("item_name", currentItem.description)
//                intent.putExtra("item_price", currentItem.price)
//                intent.putExtra("item_image", currentItem.imageUrl)
//                holder.itemView.context.startActivity(intent)
//            }

        } else {
            val shimmerViewHolder = holder as ShimmerViewHolder
            shimmerViewHolder.shimmerFrameLayout.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            shimmerItemCount // Number of shimmer placeholders
        } else {
            itemList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }
}

