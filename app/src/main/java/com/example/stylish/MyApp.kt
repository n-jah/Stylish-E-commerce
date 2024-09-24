package com.example.stylish

import android.app.Application
import com.google.firebase.FirebaseApp
class MyApp : Application() {

    companion object {
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
