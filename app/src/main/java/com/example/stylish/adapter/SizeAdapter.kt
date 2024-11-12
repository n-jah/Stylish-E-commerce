package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stylish.R
import com.example.stylish.model.home.Size

class SizeAdapter(
    private val sizes: List<Size>,
    private val onSizeSelected: (String) -> Unit  // Add this callback for selected size
) : RecyclerView.Adapter<SizeAdapter.SizeViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class SizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sizeText: TextView = itemView.findViewById(R.id.size_text_id)

        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Notify changes for re-rendering
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // Call the callback with the selected size
                onSizeSelected(sizes[selectedPosition].size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_item_size, parent, false)
        return SizeViewHolder(itemView)
    }

    override fun getItemCount(): Int = sizes.size

    override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
        val currentSize = sizes[position]
        holder.sizeText.text = currentSize.size

        // Update UI for selected item
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.selected_item_background)
            holder.sizeText.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.text_color_dark)
            )
        } else {
            holder.itemView.setBackgroundResource(R.drawable.default_item_background)
            holder.sizeText.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.text_color_light)
            )
        }
    }
}
