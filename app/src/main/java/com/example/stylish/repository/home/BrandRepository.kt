package com.example.stylish.repository.home

import androidx.lifecycle.LiveData
import com.example.stylish.model.home.Brand

interface BrandRepository {
    fun getBrands(): LiveData<List<Brand>>

}