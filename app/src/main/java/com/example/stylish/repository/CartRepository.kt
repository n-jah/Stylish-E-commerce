package com.example.stylish.repository

import com.example.stylish.model.CartItem
import com.example.stylish.modeldata.CartItemDetail

interface CartRepository {
    suspend fun addItemToCart(userId: String, cartItem: CartItem)
    suspend fun getCartItems(userId: String): List<CartItemDetail>
    suspend fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItemDetail)
    suspend fun removeItemFromCart(userId: String, cartItemId: String)
}
