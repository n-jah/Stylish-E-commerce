package com.example.stylish.repository.home

import androidx.lifecycle.LiveData
import com.example.stylish.model.home.Item

interface ItemRepsitory  {

    fun getItems():LiveData<List<Item>>
    suspend fun getItemById(itemId: String): Item?
    suspend fun updateFavoriteState(userId: String, itemId: String, isFavorite: Boolean)
    suspend fun getUserFavoriteStates(userId: String):Map<String, Boolean>
}