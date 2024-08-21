package com.example.stylish.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stylish.R
import com.example.stylish.model.Brand
import com.facebook.shimmer.ShimmerFrameLayout
import kotlin.math.log

@Suppress("DEPRECATION")
class BrandAdapter(
    private val brandList: List<Brand>,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION


    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_SHIMMER = 1

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val brandName: TextView = itemView.findViewById(R.id.brand_name)
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

            // Update UI for selected item
            if (position == selectedPosition) {
                holder.itemView.setBackgroundResource(R.drawable.selected_item_background)
                holder.brandName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_color_dark))
            } else {
                holder.itemView.setBackgroundResource(R.drawable.default_item_background)
                holder.brandName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_color_light))

            }

            // Handle item click
            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition

                // Notify changes for re-rendering
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }


        } else {
            val shimmerViewHolder = holder as ShimmerViewHolder
            shimmerViewHolder.shimmerFrameLayout.startShimmer()
        }
    }



    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            5 // Number of shimmer placeholders to show
        } else {
            brandList.size
        }
    }
}
