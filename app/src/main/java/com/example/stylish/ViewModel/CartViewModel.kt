package com.example.stylish.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.model.CartItem
import com.example.stylish.model.Item
import com.example.stylish.repository.CartRepository
import com.example.stylish.repository.ItemRepsitory
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    // Function to add an item to the cart
    fun addItemToCart(userId: String, cartItem: CartItem) {
        viewModelScope.launch {
            try {
                cartRepository.addItemToCart(userId, cartItem)

                // Optionally, refresh the cart or provide feedback after adding
            } catch (e: Exception) {

                Log.w("CartViewModel", "Error adding item to cart: ${e.message}")
            }
        }
    }
//
    fun fetchUserCart(userId: String) {
        viewModelScope.launch {
            try {
                _cartItems.value = cartRepository.getUserCart(userId)
            } catch (e: Exception) {
                // Handle error
                Log.e("CartViewModel", "Error fetching user cart: ${e.message}")
            }
        }
    }
//
//    fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItem) {
//        viewModelScope.launch {
//            try {
//                cartRepository.updateCartItem(userId, cartItemId, updatedItem)
//                fetchUserCart(userId) // Optionally refresh the cart after updating
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
//    }
//
//    fun removeItemFromCart(userId: String, cartItemId: String) {
//        viewModelScope.launch {
//            try {
//                cartRepository.removeItemFromCart(userId, cartItemId)
//                fetchUserCart(userId) // Refresh the cart after removal
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
//    }
}
