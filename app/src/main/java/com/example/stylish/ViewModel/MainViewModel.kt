package com.example.stylish.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.model.Brand
import com.example.stylish.model.Item
import com.example.stylish.repository.BrandRepository
import com.example.stylish.repository.ItemRepsitory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class MainViewModel(private val itemRepsitory: ItemRepsitory,private val brandRepository: BrandRepository) : ViewModel() {


    var userId: String? = null
    private val _items =MutableLiveData<List<Item>>()
    val items : LiveData<List<Item>> get() = _items
    val brands : LiveData<List<Brand>> = brandRepository.getBrands()
    fun fetchItemsWithFavorites() {
        viewModelScope.launch {
            val itemsList = itemRepsitory.getItems().value ?: emptyList()
            val favoriteStates = itemRepsitory.getUserFavoriteStates(userId ?: "")
            itemsList.forEach { item->
                item.isFavorite = favoriteStates[item.id] ?: false
            }
            _items.value = itemsList
            Log.w("MainViewModel", "fetchItemsWithFavorites: $itemsList")

        }
    }




    fun updateFavoriteState(itemId: String, isFavorite: Boolean){
        viewModelScope.launch {
            itemRepsitory.updateFavoriteState(userId?:"", itemId, isFavorite)

        }
    }



}
