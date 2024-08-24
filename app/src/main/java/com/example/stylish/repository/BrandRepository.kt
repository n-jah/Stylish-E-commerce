package com.example.stylish.repository

import androidx.lifecycle.LiveData
import com.example.stylish.model.Brand

interface BrandRepository {
    fun getBrands(): LiveData<List<Brand>>

}