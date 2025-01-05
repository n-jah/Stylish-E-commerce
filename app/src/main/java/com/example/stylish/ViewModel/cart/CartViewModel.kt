package com.example.stylish.ViewModel.cart
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.model.cart.CartItem
import com.example.stylish.model.cart.CartItemDetail
import com.example.stylish.model.cart.Order
import com.example.stylish.model.user.UserAddress
import com.example.stylish.repository.cart.CartRepository
import com.example.stylish.utilities.UiState
import com.example.stylish.utilities.UserUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItemDetail>>()
    val cartItems: LiveData<List<CartItemDetail>> get() = _cartItems
    private val _address = MutableLiveData<List<UserAddress>>()
    val address: LiveData<List<UserAddress>> get() = _address
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> get() = _orders

    private val _uiState = MutableLiveData<UiState<Any>>()
    val uiState: LiveData<UiState<Any>> get() = _uiState

    val userId = UserUtils.auth.currentUser?.uid ?: ""

    // Helper function to set success state
    private fun <T : Any> setSuccessState(data: T) {
        _uiState.value = UiState.Success(data)
    }

    // Add item to cart
    fun addItemToCart(cartItem: CartItem) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.addItemToCart(userId, cartItem)
                setSuccessState("Item added successfully")
            } catch (e: Exception) {
                handleException(e, "Error adding item to cart")
            }
        }
    }

// Fetch user's cart and item details
    fun loadUserCart() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val cartItemsList = cartRepository.getCartItems(userId)
                withContext(Dispatchers.Main) {
                    _cartItems.value = cartItemsList
                }
                setSuccessState(cartItemsList)
            } catch (e: Exception) {
                handleException(e, "Error loading user cart")
            }
        }
    }
    fun getTotalPrice(): Float {
        val cartItems = cartItems.value ?: emptyList()
        return cartItems.calculateTotalPrice()
    }

    fun updateCartItem(cartItemId: String, updatedItem: CartItemDetail) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.updateCartItem(userId, cartItemId, updatedItem)
                loadUserCart()
                setSuccessState("Cart item updated successfully")
            } catch (e: Exception) {
                handleException(e, "Error updating cart item")
            }
        }
    }

    // Remove item from cart
    fun removeItemFromCart(cartItemId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.removeItemFromCart(userId, cartItemId)
                loadUserCart()
                setSuccessState("Item removed successfully")
            } catch (e: Exception) {
                handleException(e, "Error removing item from cart")
            }
        }
    }

    // Clear the entire cart
    fun dropCart() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.dropCart(userId)
                loadUserCart()
                setSuccessState("Cart cleared successfully")
            } catch (e: Exception) {
                handleException(e, "Error clearing the cart")
            }

        }
    }

    // Place an order
    fun addOrder(callBack: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val calendar = Calendar.getInstance()
                val formattedDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                val cartItems = cartItems.value ?: emptyList()

                val selectedAddress = address.value?.firstOrNull()
                    ?: throw IllegalArgumentException("No address found for the user")

                val order = Order(
                    "",
                    cartItems,
                    userId,
                    formattedDate,
                    (getTotalPrice() + 10).toString(),
                    selectedAddress,
                    "Confirmed"
                )
                loadUserCart() // Refresh the cart after dropping
                cartRepository.addOrder(
                    userId,
                    cartItems,
                    formattedDate,
                    order.totalPrice,
                    address,
                    "pending"
                ) { success ->
                    if (success) {
                        try {
                            dropCart()
                            sendOrderConfirmationEmail(order)
                            setSuccessState("Order placed successfully")
                            callBack(true)
                        } catch (e: Exception) {
                            handleException(e, "Error during post-order operations")
                            callBack(false)
                        }
                    } else {
                        handleException(Exception("Order placement failed"), "Error adding order")
                        callBack(false)
                    }
                }
            } catch (e: Exception) {
                handleException(e, "Error in addOrder")
                callBack(false)
            }
        }
    }

    // Send order confirmation email
    fun sendOrderConfirmationEmail(order: Order) {
        viewModelScope.launch {
            try {
                val recipient = UserUtils.auth.currentUser?.email.toString()
                val subject = "Order Confirmation"
                val messageBody = buildOrderDetailsMessage(order)
                cartRepository.sendOrderConfirmationEmail(recipient, subject, messageBody)
            } catch (e: Exception) {
                handleException(e, "Error sending order confirmation email")
            }
        }
    }

    // Build email body
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

    // Fetch user orders
    fun getOrders() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _orders.value = cartRepository.getOrders(userId)
                setSuccessState(_orders.value ?: emptyList())
            } catch (e: Exception) {
                handleException(e, "Error fetching orders")
            }
        }
    }

    // Decrease stock for an item
    fun decreaseStock(itemId: String, size: String, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.dicresStock(itemId, size, quantity)
                setSuccessState("Stock decreased successfully")
            } catch (e: Exception) {
                handleException(e, "Error decreasing stock")
            }
        }
    }

    // Decrease stock for all items in the cart
    fun decreaseStockForAllItems(cartItems: List<CartItemDetail>) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.decreaseStockForAllItems(cartItems)
                setSuccessState("Stock decreased for all items")
            } catch (e: Exception) {
                handleException(e, "Error decreasing stock for all items")
            }
        }
    }

    // Check stock availability
    fun checkStock(itemId: String, size: String, quantity: Int): Boolean {
    var flag : Boolean
        runBlocking {
             try {
                    flag=cartRepository.checkStock(itemId, size, quantity)
            } catch (e: Exception) {
                handleException(e, "Error checking stock")
                 flag = false
                 false
            }
        }

        return flag
    }

    // Add user address
    fun addAddress(address: UserAddress) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cartRepository.addAddress(userId, address)
                setSuccessState("Address added successfully")
            } catch (e: Exception) {
                handleException(e, "Error adding address")
            }


        }
    }

    // Fetch user addresses
    fun getAddresses() {

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _address.value = cartRepository.getAddresses(userId)
                setSuccessState(_address.value ?: emptyList())
            } catch (e: Exception) {
                handleException(e, "Error fetching addresses")
            }
        }
    }

    // Centralized exception handler
    private fun handleException(e: Exception, defaultMessage: String) {
        Log.e("CartViewModel", e.message ?: defaultMessage)
        _uiState.value = UiState.Error(e.message ?: defaultMessage)
    }
    private fun Iterable<CartItemDetail>.calculateTotalPrice(): Float {
        var total = 0f
        for (item in this) {
            total += item.price * item.quantity
        }
        return total
    }


}

