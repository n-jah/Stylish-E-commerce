package com.example.stylish.ui.auth.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stylish.R
import com.example.stylish.ui.home.activity.MainActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
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
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 500)
    }

    private fun isUserSignedIn(): Boolean {
        // Implement your logic to check if the user is signed in
        // Return true if the user is signed in, false otherwise
        return false
    }
    }
