package com.example.stylish.repository

import androidx.appcompat.app.AppCompatActivity
import com.example.stylish.MyApp
import com.example.stylish.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepositoryInterface {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signUp(email: String, password: String, username: String): Result<String> {
        return try {
            val authResult = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await()

            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            val userMap = mapOf("username" to username, "email" to email)

            FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .setValue(userMap)
                .await()

            Result.success("Sign-up successful")
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthUserCollisionException -> Result.failure(Exception("Email already in use"))
                is FirebaseAuthWeakPasswordException -> Result.failure(Exception("Weak password"))
                else -> Result.failure(e)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success("Sign-in successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Boolean> {
        return try {
            FirebaseAuth.getInstance().signOut()
            Result.success(true)  // Return success result
        } catch (e: Exception) {
            Result.failure(e)  // Return failure result if any exception occurs
        }
    }

    override suspend fun firebaseAuthWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success("Google Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(MyApp.instance.getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(MyApp.instance, gso)  // Ensure App.instance is your Application context
    }

    override suspend fun firebaseAuthWithFacebook(token: String): Result<String> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            auth.signInWithCredential(credential).await()
            Result.success("Facebook Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithTwitter(authResult: AuthResult): Result<String> {
        return try {
            auth.signInWithCredential(authResult.credential!!).await()  // Ensure you handle the credential appropriately
            Result.success("Twitter Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
