package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.model.Brand
import com.facebook.shimmer.ShimmerFrameLayout

@Suppress("DEPRECATION")
class BrandAdapter(
    private var brandList: List<Brand> = emptyList(),
    private var isLoading: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_SHIMMER = 1

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val brandIcon: ImageView = itemView.findViewById(R.id.brand_img_icon)
        val brandName: TextView = itemView.findViewById(R.id.brand_name)

        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Notify changes for re-rendering
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerFrameLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_item_brand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_brand, parent, false)
            BrandViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sample_shimmer_item_brand, parent, false)
            ShimmerViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            val currentItem = brandList[position]
            val brandViewHolder = holder as BrandViewHolder
            brandViewHolder.brandName.text = currentItem.brandName

            val requestOptions = RequestOptions().transforms(CenterInside())
            Glide.with(holder.itemView.context)
                .load(currentItem.imgIcon)
                .placeholder(R.drawable.adidas)
                .apply(requestOptions)
                .into(brandViewHolder.brandIcon)

            // Update UI for selected item
            if (position == selectedPosition) {
                holder.itemView.setBackgroundResource(R.drawable.selected_item_background)
                brandViewHolder.brandName.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.text_color_dark)
                )
            } else {
                holder.itemView.setBackgroundResource(R.drawable.default_item_background)
                brandViewHolder.brandName.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.text_color_light)
                )
            }

        } else {
            val shimmerViewHolder = holder as ShimmerViewHolder
            if (isLoading) {
                shimmerViewHolder.shimmerFrameLayout.startShimmer()
            } else {
                shimmerViewHolder.shimmerFrameLayout.stopShimmer()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading || position >= brandList.size) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            5 // Number of shimmer placeholders to show
        } else {
            brandList.size
        }
    }

    // Method to update the adapter data
    fun updateBrands(newBrandList: List<Brand>) {
        this.brandList = newBrandList
        notifyDataSetChanged()
    }
}
