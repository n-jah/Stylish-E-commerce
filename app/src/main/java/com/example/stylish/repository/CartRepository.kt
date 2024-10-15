package com.example.stylish.repository

import com.example.stylish.model.CartItem

interface CartRepository {
    suspend fun addItemToCart(userId: String, cartItem: CartItem)
    suspend fun getUserCart(userId: String): List<CartItem>
//    suspend fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItem)
//    suspend fun removeItemFromCart(userId: String, cartItemId: String)
}
