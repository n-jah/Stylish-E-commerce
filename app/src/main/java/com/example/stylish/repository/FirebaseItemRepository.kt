package com.example.stylish.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.stylish.model.Item
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseItemRepository : ItemRepsitory {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val itemsRef : DatabaseReference   = firebaseDatabase.getReference("items")
    private val  _items = MutableLiveData<List<Item>>()

    override fun getItems(): LiveData<List<Item>> {
        itemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Item>()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    item?.let { lists.add(it) }
                }
                _items.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
        return _items
    }


    override suspend fun getItemById(itemId: String): Item? = withContext(Dispatchers.IO) {
        val itemSnapshot = itemsRef.child(itemId).get().await()
        itemSnapshot.getValue(Item::class.java)
    }
}