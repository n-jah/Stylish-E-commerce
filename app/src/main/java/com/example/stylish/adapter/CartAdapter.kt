package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stylish.R
import com.example.stylish.model.CartItem
import com.example.stylish.model.Item

class CartAdapter(
    private var cartItems: List<Pair<Item, CartItem>>,
    private val onQuantityChange: (CartItem) -> Unit,   // Callback for quantity change
    private val onRemoveItem: (CartItem) -> Unit        // Callback for removing item
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.productImage)
        val itemTitle: TextView = itemView.findViewById(R.id.productName)
        val itemPrice: TextView = itemView.findViewById(R.id.productPrice)
        val itemQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val increaseQuantity: Button = itemView.findViewById(R.id.btnIncrease)
        val decreaseQuantity: Button = itemView.findViewById(R.id.btnDecrease)
        val removeItemButton: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sample_cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val (item, cartItem) = cartItems[position]

        holder.itemTitle.text = item.title
        holder.itemPrice.text = item.price.toString()
        holder.itemQuantity.text = cartItem.quantity.toString()

        // Load the image using Glide
        if (item.imgUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.imgUrl[0])  // Load the first image from the image URLs
                .placeholder(android.R.drawable.ic_menu_report_image) // Placeholder image during loading
                .error(android.R.drawable.ic_menu_report_image) // Image to show in case of error
                .into(holder.itemImage)
        }

        // Increase quantity
        holder.increaseQuantity.setOnClickListener {
            cartItem.quantity += 1
            holder.itemQuantity.text = cartItem.quantity.toString()
            onQuantityChange(cartItem)
        }

        // Decrease quantity (but don't go below 1)
        holder.decreaseQuantity.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity -= 1
                holder.itemQuantity.text = cartItem.quantity.toString()
                onQuantityChange(cartItem)
            }
        }

        // Remove item
        holder.removeItemButton.setOnClickListener {
            onRemoveItem(cartItem)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    // Update data for the adapter
    fun updateData(newCartItems: List<Pair<Item, CartItem>>) {
        this.cartItems = newCartItems
        notifyDataSetChanged()
    }
}
