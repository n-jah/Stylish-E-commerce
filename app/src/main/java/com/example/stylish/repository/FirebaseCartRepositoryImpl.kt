package com.example.stylish.repository

import com.example.stylish.model.CartItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log
//import com.example.stylish.model.CartItemDetail
import com.example.stylish.modeldata.CartItemDetail

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
            val quantity = cartItemSnapshot.child("quantity").value.toString().toInt()
            val size = cartItemSnapshot.child("size").value.toString()

            // Retrieve item details from the 'items' reference
            val itemSnapshot = database.getReference("items").child(itemId).get().await()
            if (itemSnapshot.exists()) {
                val title = itemSnapshot.child("title").value.toString()
                val price = itemSnapshot.child("price").value.toString().toFloat()
                val imageUrl = itemSnapshot.child("imgUrl").value as? List<String> ?: emptyList()

                // Pass the Firebase key to CartItemDetail
                cartItemsList.add(CartItemDetail(cartItemKey, itemId, title, price, imageUrl, quantity, size))
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

    override suspend fun addOrder(userId: String, orderItems: List<CartItemDetail>) {
        try {
            val userOrdersRef = usersRef.child(userId).child("orders")
            val newOrderRef = userOrdersRef.push()
            newOrderRef.setValue(orderItems).await() // Wait for Firebase to complete the operation
            Log.d("CartRepository", "Order added successfully")
        } catch (e: Exception) {

        }


    }


}
