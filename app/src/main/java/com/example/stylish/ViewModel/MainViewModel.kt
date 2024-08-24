package com.example.stylish.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stylish.model.Brand
import com.example.stylish.model.Item
import com.example.stylish.repository.BrandRepository
import com.example.stylish.repository.ItemRepsitory
import com.google.firebase.database.*

class MainViewModel(private val itemRepsitory: ItemRepsitory,private val brandRepository: BrandRepository) : ViewModel() {

    val items : LiveData<List<Item>> = itemRepsitory.getItems()
    val brands : LiveData<List<Brand>> = brandRepository.getBrands()
}
