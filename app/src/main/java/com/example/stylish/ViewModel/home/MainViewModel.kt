package com.example.stylish.ViewModel.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.utilities.UpdateFavoriteCallback
import com.example.stylish.model.home.Brand
import com.example.stylish.model.home.Item
import com.example.stylish.model.user.User
import com.example.stylish.repository.auth.AuthRepositoryInterface
import com.example.stylish.repository.home.BrandRepository
import com.example.stylish.repository.home.ItemRepsitory
import kotlinx.coroutines.launch

class MainViewModel(private val itemRepsitory: ItemRepsitory, private val brandRepository: BrandRepository, val authRepository: AuthRepositoryInterface) : ViewModel() {


    var userId: String? = null
    private val _items =MutableLiveData<List<Item>>()
    val items : LiveData<List<Item>> get() = _items
    val brands : LiveData<List<Brand>> = brandRepository.getBrands()
    private val _favoriteItems = MutableLiveData<List<Item>>()
    val favoriteItems: LiveData<List<Item>> get() = _favoriteItems

    private val _userLiveData = MutableLiveData<User?>()
    val userLiveData: LiveData<User?> get() = _userLiveData


    fun fetchItemsWithFavorites() {
        viewModelScope.launch {
            val itemsList = itemRepsitory.getItems().value ?: emptyList()
            val favoriteStates = itemRepsitory.getUserFavoriteStates(userId ?: "")
            itemsList.forEach { item->
                item.isFavorite = favoriteStates[item.id] ?: false
            }
            _items.value = itemsList

        }
    }
    fun loadFavoriteItems() {
        viewModelScope.launch {
            val favoriteItemsList = itemRepsitory.getUserFavoriteStates(userId ?: "")
            _favoriteItems.value = favoriteItemsList.filter { it.value }.keys.mapNotNull { itemRepsitory.getItemById(it) }
        }
    }
    fun updateFavoriteState(itemId: String, isFavorite: Boolean, callback: UpdateFavoriteCallback) {
        viewModelScope.launch {
            try {
                itemRepsitory.updateFavoriteState(userId ?: "", itemId, isFavorite)

                // Update the local `_items` LiveData list
                _items.value = _items.value?.map { item ->
                    if (item.id == itemId) {
                        item.copy(isFavorite = isFavorite)
                    } else {
                        item
                    }
                }
                callback.onSuccess() // Call onSuccess if the update is successful
                Log.d("MainViewModel", "Favorite state updated successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to update favorite state", e)
                callback.onFailure() // Call onFailure if there's an error
            }
        }
    }

     fun getUserInfo() {
         authRepository.getUserInfo { user->
             _userLiveData.postValue(user)
         }
    }


}
