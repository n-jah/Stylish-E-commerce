package com.example.stylish.repository

interface AuthRepositoryInterface {
    suspend fun signUp(email: String, password: String, username: String): Result<String>
    suspend fun signIn(email: String, password: String): Result<String>
    fun signOut(): Boolean


}
