package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stylish.R
import com.example.stylish.model.cart.CartItemDetail

class OrderItemsAdapter(val orderItems: List<CartItemDetail>) : RecyclerView.Adapter<OrderItemsAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val itemName: TextView =  view.findViewById(R.id.order_item_nagem)
        val itemImage: ImageView = view.findViewById(R.id.orderImgae)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_item_layout, parent, false)
        return OrderViewHolder(view)

    }

    override fun getItemCount()= orderItems.size
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        val orderItem = orderItems[position]
        holder.itemName.text = orderItem.title
        Glide.with(holder.itemView.context).load(orderItem.imageUrl.first().toString()).into(holder.itemImage)

    }
}