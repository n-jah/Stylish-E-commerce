package com.example.stylish.model.cart
data class CartItemDetail(
    val cartItemKey: String,  // This is the Firebase key for the cart item
    val itemId: String,
    val title: String,
    val price: Float,
    val imageUrl: List<String>,
    val quantity: Int,
    val size: String,
    var isOutOfStock: Boolean = false

)

