package com.example.stylish.repository.cart

import com.example.stylish.model.cart.CartItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.stylish.model.home.Size
//import com.example.stylish.model.CartItemDetail
import com.example.stylish.model.cart.CartItemDetail
import com.example.stylish.model.cart.Order
import com.example.stylish.model.user.UserAddress
import com.example.stylish.utilities.sendEmail

class FirebaseCartRepositoryImpl : CartRepository {

    private val ordersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    private val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val database = FirebaseDatabase.getInstance()

    override suspend fun addItemToCart(userId: String, cartItem: CartItem): Unit = withContext(Dispatchers.IO) {
        try {
            val cartRef = usersRef.child(userId).child("cart")
            val snapshot = cartRef.get().await()

            var itemExists = false
            var existingCartItemKey: String? = null

            // Loop through the current cart items to check if the item with the same size exists
            for (cartItemSnapshot in snapshot.children) {
                val existingItemId = cartItemSnapshot.child("itemId").value.toString()
                val existingSize = cartItemSnapshot.child("size").value.toString()

                if (existingItemId == cartItem.itemId && existingSize == cartItem.size) {
                    // Item with the same ID and size already exists
                    itemExists = true
                    existingCartItemKey = cartItemSnapshot.key // Get the key of the existing item
                    break
                }
            }

            if (itemExists && existingCartItemKey != null) {
                // Update the quantity of the existing item
                val existingCartItemRef = cartRef.child(existingCartItemKey)
                val existingQuantity = snapshot.child(existingCartItemKey).child("quantity").value.toString().toInt()
                val updatedQuantity = existingQuantity + cartItem.quantity

                existingCartItemRef.child("quantity").setValue(updatedQuantity).await() // Update quantity in Firebase
                Log.d("CartRepository", "Item quantity updated successfully")
            } else {
                // Add a new item to the cart
                val newCartItemRef = cartRef.push()
                newCartItemRef.setValue(cartItem).await() // Add new item to Firebase
                Log.d("CartRepository", "Item added to cart successfully")

            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Error adding item to cart: ${e.message}")
            throw e // Rethrow the exception if needed
        }
    }

    override suspend fun getCartItems(userId: String): List<CartItemDetail> = withContext(Dispatchers.IO) {
        val cartItemsRef = database.getReference("users/$userId/cart")
        val snapshot = cartItemsRef.get().await()
        val cartItemsList = mutableListOf<CartItemDetail>()
        var isOutOfStock = false

        for (cartItemSnapshot in snapshot.children) {
            val cartItemKey = cartItemSnapshot.key.toString()
            val itemId = cartItemSnapshot.child("itemId").value?.toString() ?: ""
            val quantityString = cartItemSnapshot.child("quantity").value?.toString() ?: "0"
            var quantity = quantityString.toIntOrNull() ?: 0
            val sizeName = cartItemSnapshot.child("size").value?.toString() ?: ""
            // Retrieve sizes for the item
            val sizesList = mutableListOf<Size>()
            val sizesInItem = database.getReference("items/$itemId/sizes").get().await()
            var isOutOfStock = false

            if (sizesInItem.exists()) {
                Log.d("CartRepository", "Sizes retrieved successfully for item $itemId.")
                // Fetch sizes as Size objects
                sizesList.addAll(sizesInItem.children.mapNotNull { sizeSnapshot ->
                    sizeSnapshot.getValue(Size::class.java) // This line uses your original approach.
                })
                // Check if the specified size is available
                val matchedSize = sizesList.find { it.size == sizeName }

                if (matchedSize != null) {
                    // Check the stock against the requested quantity
                    if (matchedSize.stock < quantity) {
                        isOutOfStock = true
                        quantity = matchedSize.stock // Adjust quantity to available stock.
                    }
                } else {
                    isOutOfStock = true // Item size not found.
                    Log.d("CartRepository", "No matching size found for $sizeName in item $itemId.")
                }
            } else {
                Log.d("CartRepository", "Item not found in 'sizes' reference for item $itemId.")
            }

            // Retrieve other item details from the 'items' reference
            val itemSnapshot = database.getReference("items").child(itemId).get().await()
            if (itemSnapshot.exists()) {
                val title = itemSnapshot.child("title").value?.toString() ?: "Unknown Title"
                val priceString = itemSnapshot.child("price").value?.toString() ?: "0.0"
                val price = priceString.toFloatOrNull() ?: 0.0f
                val imageUrl = itemSnapshot.child("imgUrl").value as? List<String> ?: emptyList()

                Log.d("CartRepository", "Item retrieved successfully: $title, $price, $imageUrl")

                // Create CartItemDetail and add it to the list
                cartItemsList.add(CartItemDetail(cartItemKey, itemId, title, price, imageUrl, quantity, sizeName, isOutOfStock))
            } else {
                Log.d("CartRepository", "Item not found in 'items' reference for item $itemId.")
            }
        }

        Log.d("CartRepository", "Cart items retrieved successfully: $cartItemsList")
        return@withContext cartItemsList
    }
    override suspend fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItemDetail) {

        try {
            val userCartRef = usersRef.child(userId).child("cart").child(cartItemId)
            userCartRef.setValue(updatedItem).await() // Wait for Firebase to complete the operation
            Log.d("CartRepository", "Item updated in cart successfully")
        }catch (e:Exception){
            Log.e("CartRepository", "Error updating item in cart: ${e.message}")
            throw e // Handle exception if needed
        }

    }

    override suspend fun removeItemFromCart(userId: String, cartItemKey: String): Unit = withContext(Dispatchers.IO) {
        try {
            val userCartRef = usersRef.child(userId).child("cart").child(cartItemKey)
            userCartRef.removeValue().await() // Wait for Firebase to complete the operation
            Log.d("CartRepository", "Item removed from cart successfully")
        } catch (e: Exception) {
            Log.e("CartRepository", "Error removing item from cart: ${e.message}")
            throw e // Handle exception if needed
        }
    }

    override suspend fun dropCart(userId: String) {

        try {
            val userCartRef = usersRef.child(userId).child("cart")
            userCartRef.removeValue().await() // Wait for Firebase to complete the operation
            Log.d("CartRepository", "Cart dropped successfully")
        } catch (e: Exception) {
            Log.e("CartRepository", "Error dropping cart: ${e.message}")

        }

    }
    override suspend fun addOrder(
        userId: String,
        orderItems: List<CartItemDetail>,
        date: String,
        totalPrice: String,
        address: LiveData<List<UserAddress>>,
        stateOfOrder: String,
        status: (Boolean) -> Unit
    ) {
        try {
            val counterRef = FirebaseDatabase.getInstance().getReference("orderCounter") // Reference for order counter
            val newOrderRef = ordersRef.push() // Create a new order reference
            val orderRef = FirebaseDatabase.getInstance().getReference("orders") // Reference for orders

            // Fetch the current order ID counter
            val currentOrderIdSnapshot = counterRef.get().await()

            val currentOrderId = (currentOrderIdSnapshot.value as? String)?.toLongOrNull() ?: 0L

            // Increment the order ID counter for the next order
            val newOrderId = currentOrderId + 1
            counterRef.setValue(newOrderId)  // Update the counter in the database

            // Create the order object and set the new numeric order ID
            val order = Order(
                orderId = newOrderId.toString(), // Use the incremented order ID
                orderItems = orderItems,
                userId = userId,
                date = date,
                totalPrice = totalPrice,
                address = address.value!![0], // Assuming the first address is selected
                status = stateOfOrder
            )

            // Save the order to the database
            // Use the orderId as the key for the new order in the ordersRef
            orderRef.child(newOrderId.toString()).setValue(order).await()

            status(true) // Callback indicating success
            Log.d("CartRepository", "Order added successfully with ID: $newOrderId")
        } catch (e: Exception) {
            status(false) // Callback indicating failure
            Log.e("CartRepository", "Failed to add order: ${e.message}")
        }
    }


    override suspend fun getOrders(userId: String): List<Order> {
        val databaseRefForOrders = database.getReference("orders")
            .orderByChild("userId")
            .equalTo(userId)

        return try {
            val snapshot = databaseRefForOrders.get().await() // Suspend until the data is fetched
            snapshot.children.mapNotNull { it.getValue(Order::class.java) }.reversed()
        } catch (e: Exception) {
            Log.e("CartRepository", "Failed to retrieve orders: ${e.message}")
            emptyList()
        }
    }

    override suspend fun sendOrderConfirmationEmail(
        recipient: String,
        subject: String,
        messageBody: String
    ) {
        withContext(Dispatchers.IO) {
            sendEmail(recipient, subject, messageBody)
        }
    }


    override suspend fun checkStock(itemId: String, size: String, quantity: Int ): Boolean = withContext(Dispatchers.IO) {
        try {

            // Access the "items" reference to get the specific item by ID
            val itemSnapshot = database.getReference("items").child(itemId).get().await()
            if (itemSnapshot.exists()) {
                // Assuming the item has a "sizes" array field in the database
                val sizesList = itemSnapshot.child("sizes").children
                for (sizeSnapshot in sizesList) {
                    val availableSize = sizeSnapshot.child("size").value.toString()
                    val availableQuantityString = sizeSnapshot.child("quantity").value?.toString() ?: "0"
                    val availableQuantity = availableQuantityString.toIntOrNull() ?: 0
                    if (availableSize == size && availableQuantity >= quantity) {

                        return@withContext true // Stock is sufficient
                    }
                }
            }

            // Return false if item, size, or sufficient quantity is not found
            return@withContext false
        } catch (e: Exception) {
            Log.e("CartRepository", "Error checking stock: ${e.message}")
            throw e // Rethrow or handle the exception as needed
        }
    }

    override suspend fun dicresStock(itemId: String, selectedSize: String, quantityToDecress: Int): Boolean {
        try {
            // Get a reference to the specific item's sizes in Firebase
            val itemRef = database.getReference("items").child(itemId).child("sizes")
            val sizesSnapshot = itemRef.get().await()

            if (!sizesSnapshot.exists()) {
                return false
            }

            // Parse each size and find the matching size
            val sizeList = sizesSnapshot.children.mapNotNull { sizeSnapshot ->
                val sizeMap = sizeSnapshot.value as? Map<*, *>
                sizeMap?.let {
                    val size = it["size"] as? String ?: ""
                    val stock = it["quantity"] as? Long ?: 0L
                     Size(size, stock.toInt())
                }
            }
            // Find the size object that matches the given selectedSize
            val size = sizeList.find { it.size == selectedSize }

            if (size != null && size.stock >= quantityToDecress) {
                // Calculate the new stock
                val newStock = size.stock - quantityToDecress

                val sizeSnapshotKey = sizesSnapshot.children.find {
                    val sizeMap = it.value as? Map<*, *>
                    sizeMap?.get("size") == size.size
                }?.key
                if (sizeSnapshotKey != null) {
                    itemRef.child(sizeSnapshotKey).child("quantity").setValue(newStock).await()
                    return true
                } else {
                    return false
                }
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun decreaseStockForAllItems(cartItems: List<CartItemDetail>): Boolean {
        try {
            var allItemsUpdated = true  // Flag to track the success of all operations

            for (cartItem in cartItems) {
                val itemId = cartItem.itemId  // Get the item ID from the cart item
                val selectedSize = cartItem.size  // Get the selected size
                val quantityToDecrease = cartItem.quantity  // Get the quantity to decrease

                // Call the existing decreaseStock function for each item
                val itemUpdated = dicresStock(itemId, selectedSize, quantityToDecrease)

                if (!itemUpdated) {
                    // If any item fails to update, mark the flag as false and log the error
                    Log.d("CartRepository", "Failed to decrease stock for item ID: $itemId, Size: $selectedSize")
                    allItemsUpdated = false
                }
            }
            // Return the result based on whether all items were updated successfully
            if (allItemsUpdated) {
                Log.d("CartRepository", "All cart items stock decreased successfully.")
            } else {
                Log.d("CartRepository", "Some items failed to decrease stock.")
            }
            return allItemsUpdated
        } catch (e: Exception) {
            Log.e("CartRepository", "Error decreasing stock for cart items: ${e.message}")
            return false
        }
    }

    override suspend fun addAddress(userId: String, address: UserAddress) {
        try {
            val userAddressRef = usersRef.child(userId).child("address")
            userAddressRef.push().setValue(address).await()
        } catch (_: Exception) {
         }
    }
    override suspend fun getAddresses(userId: String): List<UserAddress> {
        val userAddressRef = usersRef.child(userId).child("address")
        val userAddressSnapshot = userAddressRef.get().await()

        val addressList = mutableListOf<UserAddress>()

        if (userAddressSnapshot.exists()) {
            for (addressSnapshot in userAddressSnapshot.children) {
                val address = addressSnapshot.getValue(UserAddress::class.java)
                if (address != null) {
                    addressList.add(address)

                }
            }
            Log.d("CartRepository", "Addresses retrieved successfully: $addressList")
        } else {
            Log.d("CartRepository", "No addresses found for user: $userId")
            addressList.add(UserAddress("", "", "", "", ""))
        }
        return addressList
    }

}
