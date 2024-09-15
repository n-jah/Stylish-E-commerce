package com.example.stylish.repository

interface AuthRepositoryInterface {


    fun signUp(email: String, password: String, username: String, callback: (Boolean) -> Unit)

    fun signIn(email: String, password: String, callback: (Boolean) -> Unit)

    fun signOut(callback: (Boolean) -> Unit)

}
