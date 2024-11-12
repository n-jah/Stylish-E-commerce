package com.example.stylish.ViewModel.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.repository.payment.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentViewModel(private val paymentRepository: PaymentRepository) : ViewModel() {

    private val _clientSecret = MutableLiveData<String>()
    val clientSecret: LiveData<String> get() = _clientSecret

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun createPaymentFlow(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            paymentRepository.createCustomer(
                onSuccess = { customerId ->
                    paymentRepository.createEphemeralKey(
                        customerId,
                        onSuccess = { ephemeralKey ->
                            paymentRepository.createPaymentIntent(
                                customerId, ephemeralKey, amount,
                                onSuccess = { secret -> _clientSecret.postValue(secret) },
                                onError = { errorMessage -> _error.postValue(errorMessage) }
                            )
                        },
                        onError = { errorMessage -> _error.postValue(errorMessage) }
                    )
                },
                onError = { errorMessage -> _error.postValue(errorMessage) }
            )
        }
    }

}