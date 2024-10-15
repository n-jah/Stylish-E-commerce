package com.example.stylish.repository

import com.example.stylish.model.CartItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log

class FirebaseCartRepositoryImpl : CartRepository {

    private val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    override suspend fun addItemToCart(userId: String, cartItem: CartItem): Unit = withContext(Dispatchers.IO) {
        try {
            val userCartRef = usersRef.child(userId).child("cart").push()
            userCartRef.setValue(cartItem).await() // Wait for Firebase response
            Log.d("CartRepository", "Item added to cart successfully")
        } catch (e: Exception) {
            Log.e("CartRepository", "Error adding item to cart: ${e.message}")
            throw e // Rethrow the exception if needed
        }
    }




    override suspend fun getUserCart(userId: String): List<CartItem> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cartSnapshot = usersRef.child(userId).child("cart").get().await() // Get data using coroutines
            val cartItems = mutableListOf<CartItem>()
            for (itemSnapshot in cartSnapshot.children) {
                itemSnapshot.getValue(CartItem::class.java)?.let { cartItems.add(it) }
            }
            cartItems
        } catch (e: Exception) {
            Log.e("CartRepository", "Error retrieving user cart: ${e.message}")
            emptyList() // Return an empty list in case of error
        }
    }

//    override suspend fun updateCartItem(userId: String, cartItemId: String, updatedItem: CartItem) = withContext(Dispatchers.IO) {
//        try {
//            val userCartRef = usersRef.child(userId).child("cart").child(cartItemId)
//            userCartRef.setValue(updatedItem).await() // Update the item in Firebase
//        } catch (e: Exception) {
//            Log.e("CartRepository", "Error updating cart item: ${e.message}")
//            throw e // Rethrow the exception if needed
//        }
//    } // Return type is Unit
//
//    override suspend fun removeItemFromCart(userId: String, cartItemId: String) = withContext(Dispatchers.IO) {
//        try {
//            val userCartRef = usersRef.child(userId).child("cart").child(cartItemId)
//            userCartRef.removeValue().await() // Remove the item from Firebase
//        } catch (e: Exception) {
//            Log.e("CartRepository", "Error removing item from cart: ${e.message}")
//            throw e // Rethrow the exception if needed
//        }
    }
// Return type is Unit

