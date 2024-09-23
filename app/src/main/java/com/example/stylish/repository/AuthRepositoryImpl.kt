package com.example.stylish.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepositoryInterface {

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

    override fun signOut(): Boolean {
        return try {
            FirebaseAuth.getInstance().signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun firebaseAuthWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            Result.success("Google Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
