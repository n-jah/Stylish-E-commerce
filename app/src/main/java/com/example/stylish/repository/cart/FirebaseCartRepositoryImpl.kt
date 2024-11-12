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

class FirebaseCartRepositoryImpl : CartRepository {

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

        for (cartItemSnapshot in snapshot.children) {
            val cartItemKey = cartItemSnapshot.key.toString() // Get the Firebase key
            val itemId = cartItemSnapshot.child("itemId").value.toString()
            var quantity = cartItemSnapshot.child("quantity").value.toString().toInt()
            val size = cartItemSnapshot.child("size").value.toString()
            var isOutOfStock = false

            val sizesInItem = database.getReference("items/$itemId/sizes").get().await()
            if(sizesInItem.exists()){
                sizesInItem.children.forEach {

                    val sizeOfItem = it.child("size").value.toString()
                    val quantityOfItem = it.child("quantity").value.toString().toInt()

                    if (sizeOfItem == size){

                        quantity = if (quantityOfItem == 0){
                            isOutOfStock = true
                            quantityOfItem
                        }else if (quantityOfItem < quantity){
                            quantityOfItem
                        }else{
                            cartItemSnapshot.child("quantity").value.toString().toInt()
                        }
                    }

                }
            }


            // Retrieve item details from the 'items' reference
            val itemSnapshot = database.getReference("items").child(itemId).get().await()
            if (itemSnapshot.exists()) {
                val title = itemSnapshot.child("title").value.toString()
                val price = itemSnapshot.child("price").value.toString().toFloat()
                val imageUrl = itemSnapshot.child("imgUrl").value as? List<String> ?: emptyList()

                // Pass the Firebase key to CartItemDetail
                cartItemsList.add(CartItemDetail(cartItemKey, itemId, title, price, imageUrl, quantity, size,isOutOfStock))
            }
        }
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
        status: (Boolean) -> Unit
    ) {
        try {
            val userOrdersRef = usersRef.child(userId).child("orders")
            val newOrderRef = userOrdersRef.push()
            val oreder = Order(orderItems, userId, date, totalPrice, address.value!![0])
            newOrderRef.setValue(oreder).await()
            status(true)

            Log.d("CartRepository", "Order added successfully")
        } catch (e: Exception) {
            status(false)  // Invoke status callback with false on failure
            Log.e("CartRepository", "Failed to add order: ${e.message}")
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
                    val availableQuantity = sizeSnapshot.child("quantity").value.toString().toInt()
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
