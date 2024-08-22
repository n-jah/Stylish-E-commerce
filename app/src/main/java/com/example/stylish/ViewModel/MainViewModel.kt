package com.example.stylish.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stylish.model.Item
import com.google.firebase.database.*

class MainViewModel : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    private val itemsRef: DatabaseReference = firebaseDatabase.getReference("items")
    private var itemsListener: ValueEventListener? = null

    fun loadItems() {
        itemsListener = itemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Item>()
                for (itemSnapshot in snapshot.children) {
                    val list = itemSnapshot.getValue(Item::class.java)
                    list?.let { lists.add(it) }
                }
                _items.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, e.g., log it or show a message to the user
                  Log.e("MainViewModel", "Failed to load items", error.toException())
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the listener to prevent memory leaks
        itemsListener?.let { itemsRef.removeEventListener(it) }
    }
}
