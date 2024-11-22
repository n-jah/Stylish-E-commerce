package com.example.stylish.ViewModel.cart

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.model.cart.CartItem
//import com.example.stylish.model.CartItemDetail
import com.example.stylish.model.cart.CartItemDetail
import com.example.stylish.model.cart.Order
import com.example.stylish.model.user.UserAddress
import com.example.stylish.repository.cart.CartRepository
import com.example.stylish.utilities.UserUtils
import com.example.stylish.utilities.sendEmail
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItemDetail>>()
    val cartItems: LiveData<List<CartItemDetail>> get() = _cartItems
    private val _address = MutableLiveData<List<UserAddress>>()
    val address: LiveData<List<UserAddress>> get() = _address
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> get() = _orders
    val userId = UserUtils.auth.currentUser?.uid ?: ""
    // Function to add an item to the cart
    fun addItemToCart( cartItem: CartItem) {
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
// Fetch user's cart and item details
    fun loadUserCart() {
        viewModelScope.launch {
            try {
                // Step 1: Fetch cart items with just ID, quantity, and size
                val cartItemsList = cartRepository.getCartItems(userId)

                // Step 2: Update LiveData with the cart details to be displayed in UI
                _cartItems.value = cartItemsList
            } catch (e: Exception) {
                // Handle errors appropriately (e.g., show error message to the user)
                e.printStackTrace()
            }
        }
    }

    fun getTotalPrice(): Float {


        var cartItems = cartItems.value ?: emptyList()
        var totalPrice = 0f
        for (item in cartItems) {
            totalPrice += item.price * item.quantity

        }
        return totalPrice
    }
//


    fun updateCartItem( cartItemId: String, updatedItem: CartItemDetail) {
        viewModelScope.launch {
            try {
                cartRepository.updateCartItem(userId, cartItemId, updatedItem)
                loadUserCart() // Optionally refresh the cart after updating
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeItemFromCart( cartItemId: String) {
        viewModelScope.launch {
            try {
                cartRepository.removeItemFromCart(userId, cartItemId)
                loadUserCart() // Refresh the cart after removal
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun dropCart() {
        viewModelScope.launch {
            try {
                cartRepository.dropCart(userId)
                loadUserCart() // Refresh the cart after dropping
            } catch (e: Exception) {
                // Handle error
            }

        }
    }

    fun addOreder( callBack: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Get the current date compatible with all API levels
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val formattedDate = "$year-$month-$day" // Format the date as needed
                val cartItems = cartItems.value ?: emptyList()

                val order = Order(cartItems, userId, formattedDate, (getTotalPrice()+10).toString(), address.value!![0], "Confirmed")
                loadUserCart() // Refresh the cart after dropping
                cartRepository.addOrder(
                    userId,
                    cartItems,
                    formattedDate,
                    (getTotalPrice()+10).toString(),
                    address,
                    "Confirmed"
                ) { success ->
                    if (success) {
                        dropCart()
                        sendOrderConfirmationEmail(order)
                        callBack(true)
                    } else {
                        Log.d("CartViewModel", "Error adding order")
                        callBack(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error in addOrder: ${e.message}")
                callBack(false)
            }
        }
    }

    fun sendOrderConfirmationEmail(order: Order) {
        viewModelScope.launch {
            try {
                val recipient = UserUtils.auth.currentUser?.email.toString()
//                val recipient = "be-ngah@outlook.com"
                val subject = "Order Confirmation"
                val messageBody = buildOrderDetailsMessage(order)
                cartRepository.sendOrderConfirmationEmail(recipient, subject, messageBody)
                Log.d("CartViewModel", "Order confirmation email sent successfully${orders.value?.last()}")
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error sending order confirmation email: ${e.message}")
            }
        }
    }
    private fun buildOrderDetailsMessage(order: Order): String {
        val items = order.orderItems.joinToString(separator = "\n") { item ->
            "${item.title} (Size: ${item.size}) - $${item.price} x ${item.quantity}"
        }
        return """
        Thank you for your order!
        
        Order Date: ${order.date}
        Total Price: $${order.totalPrice}
        
        Address:
        ${order.address.name}
        ${order.address.detailedAddress}, ${order.address.government}, ${order.address.country}
        
        Items:
        $items
        
        Your order is confirmed and will be delivered soon.
    """.trimIndent()
    }

    fun getOrders(){
        viewModelScope.launch {
            try {
                _orders.value = cartRepository.getOrders(userId)
                Log.d("CartViewModel", "Orders fetched successfully${_orders.value}")
            } catch (e: Exception) {
                // Handle error
    }
        }
    }
    fun dicresStock(itemId: String, selectedSize: String, quantityToDecress: Int){
        viewModelScope.launch {
            try {
                cartRepository.dicresStock(itemId, selectedSize, quantityToDecress)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    fun decreaseStockForAllItems(cartItems: List<CartItemDetail>) {
        viewModelScope.launch {
            try {
                cartRepository.decreaseStockForAllItems(cartItems)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun checkStock(itemId: String, size: String, quantity: Int): Boolean {
        var stock = false

        // Use async to return the value
        runBlocking {
            try {
                // Wait for the result from the repository
                stock = cartRepository.checkStock(itemId, size, quantity)

            } catch (_: Exception) {
                // Handle exceptions
            }
        }

        return stock
    }

    fun addAddress(address: UserAddress) {
        viewModelScope.launch {
            try {
                cartRepository.addAddress(userId, address)
            } catch (e: Exception) {
            }


        }
    }
    fun getAddresses(){

        viewModelScope.launch {
            try {
                _address.value = cartRepository.getAddresses(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }

    }








}



