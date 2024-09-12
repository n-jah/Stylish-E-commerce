package com.example.stylish.ui.home.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.stylish.R
import com.example.stylish.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.addressAdd.setOnClickListener{
            startActivity(Intent(this,AddressActivity::class.java))
        }
        binding.backButton.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }


    }
}