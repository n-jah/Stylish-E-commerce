package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stylish.R
import com.example.stylish.model.cart.Order

class OrdersAdapter(private val orders: MutableList<Order>) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        if(order.status == "on_delivery"){
            holder.itemView.setBackgroundResource(R.drawable.background_card_complited)
            holder.status.text = "Status: On delivery"

        }else{
            holder.status.text = "Status: Confirmed "
            holder.itemView.setBackgroundResource(R.drawable.background_card_panding)
        }
        // Set the order details: total price, address, and date
        holder.totalPrice.text = "Total Price: ${order.totalPrice} $"
        holder.address.text = "Address: ${order.address.country} - ${order.address.government} - ${order.address.detailedAddress}"
        holder.date.text = "Date: ${order.date}"

        val orderItemsAdapter = OrderItemsAdapter(order.orderItems)
        // Set up the nested RecyclerView with order items
        holder.itemsRecyclerView.layoutManager = LinearLayoutManager(holder.itemsRecyclerView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.itemsRecyclerView.adapter = orderItemsAdapter




    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val totalPrice: TextView = view.findViewById(R.id.orderTotal)
        val address: TextView = view.findViewById(R.id.orderAddress)
        val date: TextView = view.findViewById(R.id.orderDate)
        val itemsRecyclerView: RecyclerView = view.findViewById(R.id.orderItemsRecyclerView)
        val status: TextView = view.findViewById(R.id.orderStatus)

    }
}
