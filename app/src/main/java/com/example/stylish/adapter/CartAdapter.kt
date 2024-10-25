package com.example.stylish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stylish.R
//import com.example.stylish.model.CartItemDetail
import com.example.stylish.modeldata.CartItemDetail

class CartAdapter(
    private var cartItems: MutableList<CartItemDetail>,
    private val onQuantityChange: (CartItemDetail, String) -> Unit,  // Added cartItemKey
    private val onRemoveItem: (CartItemDetail) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // View bindings
        val imageView: ImageView = itemView.findViewById(R.id.productImage)
        val title: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val quantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val size: TextView = itemView.findViewById(R.id.productSize)
        val increaseButton: AppCompatImageButton = itemView.findViewById(R.id.btnIncrease)
        val decreaseButton: AppCompatImageButton = itemView.findViewById(R.id.btnDecrease)
        val removeButton: AppCompatImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.sample_cart_item, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        // Binding data to views
        holder.title.text = cartItem.title
        holder.price.text = cartItem.price.toString()
        holder.quantity.text = cartItem.quantity.toString()
        holder.size.text = cartItem.size.capitalize()

        // Load image if available
        if (cartItem.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(cartItem.imageUrl[0])
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        }
//
        // Increase quantity
        holder.increaseButton.setOnClickListener {
            val updatedCartItem = cartItem.copy(quantity = cartItem.quantity + 1)
            onQuantityChange(updatedCartItem, cartItem.cartItemKey)

        }


//        // Decrease quantity
        holder.decreaseButton.setOnClickListener{
            var quantity = cartItem.quantity
            if (quantity > 1) {

                val updatedCartItem = cartItem.copy(quantity = quantity - 1)
                onQuantityChange(updatedCartItem, cartItem.cartItemKey)

            }
        }


        // Remove item
        holder.removeButton.setOnClickListener {
            onRemoveItem(cartItem)
            removeItem(cartItem) // Call the adapter's own removeItem method
        }
    }

    override fun getItemCount(): Int = cartItems.size

    // Method to update cart items
    fun updateCartItems(newCartItems: List<CartItemDetail>) {
        this.cartItems = newCartItems.toMutableList()
        notifyDataSetChanged()
    }

    // Method to remove a cart item
    fun removeItem(cartItem: CartItemDetail) {
        val position = cartItems.indexOf(cartItem)
        if (position != -1) {
            cartItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
        }
    }

    // Accessor to expose cartItems list size
    fun getCartItems(): List<CartItemDetail> {
        return cartItems
    }
}
