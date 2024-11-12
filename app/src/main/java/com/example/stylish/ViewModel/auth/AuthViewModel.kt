package com.example.stylish.ViewModel.auth

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stylish.repository.auth.AuthRepositoryInterface
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepositoryInterface) : ViewModel() {

    private val _signUpResult = MutableLiveData<Result<String>>()
    val signUpResult: LiveData<Result<String>> get() = _signUpResult

    private val _restPassword = MutableLiveData<Result<String>>()
    val  restPassword: LiveData<Result<String>> get() = _restPassword

    private val _isRegistered = MutableLiveData<Result<String>>()
    val isRegistered: LiveData<Result<String>> get() = _isRegistered


    private val _signInResult = MutableLiveData<Result<String>>()
    val signInResult: LiveData<Result<String>> get() = _signInResult

    private val _signOutResult = MutableLiveData<Result<Boolean>>()
    val signOutResult: LiveData<Result<Boolean>> get() = _signOutResult

    private val _googleSignInResult = MutableLiveData<Result<String>>()
    val googleSignInResult: LiveData<Result<String>> get() = _googleSignInResult

    private val _facebookSignInResult = MutableLiveData<Result<String>>()
    val facebookSignInResult: LiveData<Result<String>> get() = _facebookSignInResult

    private val _twitterSignInResult = MutableLiveData<Result<AuthResult>>()
    val twitterSignInResult: LiveData<Result<AuthResult>> get() = _twitterSignInResult


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
        viewModelScope.launch {
            val result = authRepository.signOut()  // Await the result
            _signOutResult.postValue(result)
        }
    }

    fun initiateGoogleSignIn(fragment: Fragment, requestCode: Int) {
        val googleSignInClient = authRepository.getGoogleSignInClient()
        fragment.startActivityForResult(googleSignInClient.signInIntent, requestCode)
    }

    fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                viewModelScope.launch {
                    _googleSignInResult.postValue(authRepository.firebaseAuthWithGoogle(idToken))
                }
            }
        } catch (e: ApiException) {
            _googleSignInResult.postValue(Result.failure(e))
        }
    }

    fun handleFacebookSignInResult(token: String) {
        viewModelScope.launch {
            _facebookSignInResult.postValue(authRepository.firebaseAuthWithFacebook(token))
        }
    }

    fun handleTwitterSignInResult(authResult: AuthResult) {
        viewModelScope.launch {
            _twitterSignInResult.postValue(Result.success(authResult))
        }
    }

    fun restorePasswordWithEmail(email: String) {
        viewModelScope.launch {
            try {
                val result = authRepository.restorePasswordWithEmail(email)
                _restPassword.postValue(result)
            } catch (e: Exception) {
                _restPassword.postValue(Result.failure(e))

            }
        }

    }



}
