package com.example.stylish.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.stylish.model.Item

class ItemDiffCallback: DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {

        return oldItem == newItem

    }
}