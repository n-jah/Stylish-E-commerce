package com.example.stylish.ViewModel.home

import android.net.Uri
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
import com.example.stylish.utilities.UserUtils
import kotlinx.coroutines.launch

class MainViewModel(private val itemRepsitory: ItemRepsitory, private val brandRepository: BrandRepository, val authRepository: AuthRepositoryInterface) : ViewModel() {


    var userId: String? = null
    private val _items =MutableLiveData<List<Item>>()
    val items : LiveData<List<Item>> get() = _items
    val brands : LiveData<List<Brand>> = brandRepository.getBrands()

    private val _favoriteItems = MutableLiveData<List<Item>>()
    val favoriteItems: LiveData<List<Item>> get() = _favoriteItems

    private val _imageURL = MutableLiveData<String?>()
    val imageURL: MutableLiveData<String?> get() = _imageURL

    private val _userDataLiveData = MutableLiveData<User?>()
    val userDataLiveData: LiveData<User?> get() =_userDataLiveData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        fetchItemsWithFavorites()
        loadFavoriteItems()
        getUserData()

    }
    fun getUserData() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val userDataResult = authRepository.getUserData { isLoading ->
                    _loading.value = isLoading
                }
                if (userDataResult.isSuccess) {
                    _userDataLiveData.value = userDataResult.getOrNull() // Update LiveData
                    _loading.value = false
                } else {
                    _userDataLiveData.value = null
                }
            } catch (e: Exception) {
                _userDataLiveData.value = null
                _loading.value= false
            } finally {
                _loading.value = false
            }
        }
    }
    fun  uploadImgProfileReturnUrl(uri : String){
        _loading.value = true
        viewModelScope.launch {
            try{
                val urlResult = authRepository.uploadImgProfileReturnUrl(uri) { loading ->
                    _loading.value = loading
                }
                if (urlResult.isSuccess){
                    _imageURL.value = urlResult.getOrNull()
                    updateUserImageUrl(urlResult.getOrNull().toString())
                    Log.d("MainViewModel", "Image URL: ${urlResult.getOrNull()}")
                }else{
                    _imageURL.value = null
                }
            }catch (e: Exception){
                _imageURL.value = null
                _loading.value = false
            }

        }
        _loading.value = false
    }

    fun updateUserImageUrl(Url: String){
        _loading.value = true
        viewModelScope.launch {
            try{
              authRepository.updateProfilePicUrl(Url) { loading ->
                    _loading.value = loading
                }

            }catch (e: Exception){
                 _loading.value = false
            }

        }
        _loading.value = false
    }
    fun updateUserName(userName : String){
        _loading.value = true
        viewModelScope.launch {
            try{
                authRepository.updateUserName(userName) { loading ->
                    _loading.value = loading
                }
                _loading.value = false
            }catch (e: Exception){
                _loading.value = false
            }
            _loading.value = false

        }
    }

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



}
