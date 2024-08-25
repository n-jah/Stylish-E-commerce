package com.example.stylish.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.ViewModel.MainViewModel

class MainViewModelFactory(
    private val itemRepository: ItemRepsitory,
    private val brandRepository: BrandRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(itemRepository, brandRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
