package com.example.stylish.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stylish.model.User
import com.example.stylish.repository.AuthRepositoryInterface
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(private val authRepository: AuthRepositoryInterface):ViewModel() {
    private val _signUpResult = MutableLiveData<Boolean>()
    val signUpResult: LiveData<Boolean> get()  = _signUpResult

    private val _signInResult = MutableLiveData<Boolean>()
    val signInResult: LiveData<Boolean> get()  = _signInResult

    private val _signOutResult = MutableLiveData<Boolean>()
    val signOutResult: LiveData<Boolean> get()  = _signOutResult

    fun signUpUser(email: String, password: String, username: String) {
        authRepository.signUp(email, password, username) { success ->
            _signUpResult.postValue(success)
        }

    }
    fun signInUser(email: String, password: String) {

        authRepository.signIn(email, password) { success ->
            _signInResult.postValue(success)
        }

    }
    fun signOutUser() {
        authRepository.signOut() { success ->
            _signOutResult.postValue(success)
        }
    }
}