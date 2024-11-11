package com.example.stylish.repository

import com.example.stylish.model.CartItem
import com.example.stylish.modeldata.CartItemDetail

interface CartRepository {
    suspend fun addItemToCart(userId: String, cartItem: CartItem)
    suspend fun getCartItems(userId: String): List<CartItemDetail>
    suspend fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItemDetail)
    suspend fun removeItemFromCart(userId: String, cartItemId: String)
    suspend fun dropCart(userId: String)
    suspend fun addOrder(userId: String,orderItems: List<CartItemDetail>,status : (Boolean)-> Unit)
    suspend fun checkStock(itemId: String, size: String, quantity: Int): Boolean
    suspend fun dicresStock(itemId: String, selectedSize: String, quantityToDecress: Int): Boolean
    suspend fun decreaseStockForAllItems(cartItems: List<CartItemDetail>): Boolean
}
