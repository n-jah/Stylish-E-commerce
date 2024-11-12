package com.example.stylish.repository.auth

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
   fun getUserInfo(callback:(user: User?)->Unit)
}
