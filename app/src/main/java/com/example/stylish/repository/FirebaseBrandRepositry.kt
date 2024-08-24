package com.example.stylish.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.stylish.model.Brand
import com.example.stylish.model.Item
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseBrandRepositry : BrandRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val brandsRef : DatabaseReference = firebaseDatabase.getReference("brands")
    private val _brands = MutableLiveData<List<Brand>>()


    override fun getBrands(): LiveData<List<Brand>> {
        brandsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<Brand>()
                for (itemSnapshot in snapshot.children) {
                    val brand = itemSnapshot.getValue(Brand::class.java)
                    brand?.let { lists.add(it) }
                }
                _brands.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
        return _brands
    }





}