package com.example.stylish.ui.auth.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stylish.R
import com.example.stylish.ui.home.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    companion object {
        const val PREFS_NAME = "userPrefs"
        const val REMEMBER_ME_KEY = "keepSignedIn"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Ensure the splash screen appears full-screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Delay for a few seconds before starting the next activity
        Handler().postDelayed({
            // Check if the user is signed in
            if (isUserSignedIn()) {
                // If signed in, navigate to the main activity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // If not signed in, navigate to the sign-in or sign-up activity
                startActivity(Intent(this, WellcomeScreen::class.java))
            }
            finish()
        }, 1000)
    }

    private fun isUserSignedIn(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rememberMe = sharedPreferences.getBoolean(REMEMBER_ME_KEY, false)
        return rememberMe && isFirebaseUserAuthenticated()

    }

    // Helper function to check if Firebase user is signed in (implement as per your auth logic)
    private fun isFirebaseUserAuthenticated(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d("SplashScreen", "User is signed in: $currentUser")
        } else {
            Log.d("SplashScreen", "No user is signed in.")
        }
        return currentUser != null
    }


}
