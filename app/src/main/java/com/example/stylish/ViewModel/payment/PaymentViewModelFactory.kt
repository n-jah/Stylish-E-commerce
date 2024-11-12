package com.example.stylish.ViewModel.payment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.repository.payment.PaymentRepository

class PaymentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = PaymentRepository(context)
        return PaymentViewModel(repository) as T
    }
}