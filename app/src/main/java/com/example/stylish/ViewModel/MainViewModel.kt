package com.example.stylish.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stylish.model.Item
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainViewModel() : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val _items = MutableLiveData<List<Item>>()
    private val items: LiveData<List<Item>> = _items

    fun loadItems() {
        val itemsRef = firebaseDatabase.getReference("items")

        itemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Item>()
                for (itemSnapshot in snapshot.children) {
                    val list = itemSnapshot.getValue(Item::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _items.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        )

    }


}
