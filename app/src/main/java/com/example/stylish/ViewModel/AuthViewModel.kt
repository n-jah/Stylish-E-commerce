package com.example.stylish.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.model.User
import com.example.stylish.repository.AuthRepositoryInterface
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepositoryInterface):ViewModel() {

    private val _signUpResult = MutableLiveData<Result<String>>()
    val signUpResult: LiveData<Result<String>> get() = _signUpResult

    private val _signInResult = MutableLiveData<Result<String>>()
    val signInResult: LiveData<Result<String>> get() = _signInResult

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult: LiveData<Boolean> get() = _signOutResult



    fun signUpUser(email: String, password: String, username: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.signUp(email, password, username)
                _signUpResult.postValue(result)
            } catch (e: Exception) {
                _signUpResult.postValue(Result.failure(e))
            }
        }
    }
    fun signInUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.signIn(email, password)
                _signInResult.postValue(result)
            } catch (e: Exception) {
                _signInResult.postValue(Result.failure(e))
            }
        }
    }

    fun signOutUser() {
        val success = authRepository.signOut()
        _signOutResult.postValue(success)
    }
}