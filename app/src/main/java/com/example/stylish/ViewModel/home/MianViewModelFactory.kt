package com.example.stylish.ViewModel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.repository.auth.AuthRepositoryInterface
import com.example.stylish.repository.home.BrandRepository
import com.example.stylish.repository.home.ItemRepsitory

class MainViewModelFactory(
    private val itemRepository: ItemRepsitory,
    private val brandRepository: BrandRepository,
    private val authRepository: AuthRepositoryInterface
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(itemRepository, brandRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
