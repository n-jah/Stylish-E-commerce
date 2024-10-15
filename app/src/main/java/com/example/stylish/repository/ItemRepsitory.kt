package com.example.stylish.repository

import androidx.lifecycle.LiveData
import com.example.stylish.model.Item

interface ItemRepsitory  {

    fun getItems():LiveData<List<Item>>
    suspend fun getItemById(itemId: String): Item?

}