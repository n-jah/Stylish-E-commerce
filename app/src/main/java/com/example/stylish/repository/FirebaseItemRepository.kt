package com.example.stylish.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.stylish.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
class FirebaseItemRepository : ItemRepsitory {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val itemsRef : DatabaseReference   = firebaseDatabase.getReference("items")
    private val userFavoritesRef: DatabaseReference = firebaseDatabase.getReference("users") // Reference to users
    private val  _items = MutableLiveData<List<Item>>()

    override fun getItems(): LiveData<List<Item>> {
        itemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lists = mutableListOf<Item>()
                for (itemSnapshot in dataSnapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    item?.let { lists.add(it) }
                }
                Log.w("FirebaseItemRepository", "lists: $lists")
                _items.value = lists // Update LiveData
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseItemRepository", "DatabaseError: ${databaseError.message}")
            }
        })
        return _items
    }

    override suspend fun getItemById(itemId: String): Item? = withContext(Dispatchers.IO) {
        val itemSnapshot = itemsRef.child(itemId).get().await()
        itemSnapshot.getValue(Item::class.java)
    }

    override suspend fun getUserFavoriteStates(userId: String): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val favoriteStates = mutableMapOf<String, Boolean>()
        val userFavoritesSnapshot = userFavoritesRef.child(userId).child("favorite").get().await()
        for (snapshot in userFavoritesSnapshot.children) {
            favoriteStates[snapshot.key!!] = true // If the key exists, it's a favorite
        }
        return@withContext favoriteStates
    }

    override suspend fun updateFavoriteState(userId: String, itemId: String, isFavorite: Boolean): Unit = withContext(Dispatchers.IO) {
        if (isFavorite){
            userFavoritesRef.child(userId).child("favorite").child(itemId).setValue(true)
        }else{
            userFavoritesRef.child(userId).child("favorite").child(itemId).removeValue()
        }

    }
}