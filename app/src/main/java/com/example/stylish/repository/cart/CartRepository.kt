package com.example.stylish.repository.cart

import androidx.lifecycle.LiveData
import com.example.stylish.model.cart.CartItem
import com.example.stylish.model.cart.CartItemDetail
import com.example.stylish.model.cart.Order
import com.example.stylish.model.user.UserAddress

interface CartRepository {
    suspend fun addItemToCart(userId: String, cartItem: CartItem)
    suspend fun getCartItems(userId: String): List<CartItemDetail>
    suspend fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItemDetail)
    suspend fun removeItemFromCart(userId: String, cartItemId: String)
    suspend fun dropCart(userId: String)
    suspend fun addOrder(
        userId: String, orderItems: List<CartItemDetail>,
        date: String, totalPrice: String,
        address: LiveData<List<UserAddress>>,stateOfOrder: String, status: (Boolean)-> Unit)
    suspend fun getOrders(userId: String): List<Order>
    suspend fun checkStock(itemId: String, size: String, quantity: Int): Boolean
    suspend fun dicresStock(itemId: String, selectedSize: String, quantityToDecress: Int): Boolean
    suspend fun decreaseStockForAllItems(cartItems: List<CartItemDetail>): Boolean
    suspend fun addAddress(userId: String, address: UserAddress)
    suspend fun getAddresses(userId: String): List<UserAddress>
    suspend fun sendOrderConfirmationEmail(recipient: String, subject: String, messageBody: String)


}
