package com.example.stylish.repository.auth

import android.net.Uri
import com.example.stylish.model.user.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthResult

interface AuthRepositoryInterface {
    suspend fun signUp(email: String, password: String, username: String): Result<String>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signOut(): Result<Boolean>  // Changed return type to Result<Boolean>
    suspend fun firebaseAuthWithGoogle(idToken: String): Result<String>
    fun getGoogleSignInClient(): GoogleSignInClient
    suspend fun firebaseAuthWithFacebook(token: String): Result<String>
    suspend fun signInWithTwitter(authResult: AuthResult): Result<String>
   suspend fun restorePasswordWithEmail(email: String): Result<String>
    suspend fun getUserData(onLoading: (Boolean) -> Unit): Result<User>
    suspend fun updateProfilePicUrl(imageUrl: String, loading: (Boolean) -> Unit)
    suspend fun updateUserName(userName: String, loading: (Boolean) -> Unit)
    suspend fun uploadImgProfileReturnUrl(
        imageUri: String,
        loading: (Boolean) -> Unit
    ): Result<String>
}
