package com.example.stylish.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthRepositoryImpl: AuthRepositoryInterface  {
    override fun signUp(
        email: String,
        password: String,
        username: String,
        callback: (Boolean) -> Unit
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val userMap = mapOf(
                        "username" to username,
                        "email" to email
                    )

                    // Storing user info in Firebase Realtime Database
                    FirebaseDatabase.getInstance().getReference("users").child(userId!!)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            callback(true)
                        }.addOnFailureListener {
                            callback(false)
                        }
                } else {
                    callback(false)
                }
            }
    }

    override fun signIn(email: String,
                        password: String,
                        callback: (Boolean) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true)


            } else {
                callback(false)

            }
        }

     }

    override fun signOut(callback: (Boolean) -> Unit)
    {
        FirebaseAuth.getInstance().signOut()
        callback(true)
    }
}