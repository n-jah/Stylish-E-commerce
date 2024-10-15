package com.example.stylish.ui.home.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.R
import com.example.stylish.ViewModel.CartViewModel
import com.example.stylish.adapter.CartAdapter
import com.example.stylish.databinding.ActivityCartBinding
import com.google.firebase.auth.FirebaseAuth

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)

        auth = FirebaseAuth.getInstance()
//
//        // Set up RecyclerView and adapter
//        cartAdapter = CartAdapter(emptyList(), onQuantityChange = { cartItem ->
//            // Pass userId and cartItemId correctly to the ViewModel
//            viewModel.updateCartItem(auth.currentUser?.uid ?: "", cartItem.itemId, cartItem)
//        }, onRemoveItem = { cartItem ->
//            // Pass userId and cartItemId correctly to the ViewModel
//            viewModel.removeItemFromCart(auth.currentUser?.uid ?: "", cartItem.itemId)
//        })
//
//        binding.cartRecyclerView.apply {
//            layoutManager = LinearLayoutManager(this@CartActivity)
//            adapter = cartAdapter
//        }
//
//        // Observe the cart items from ViewModel
//        viewModel.cartItems.observe(this) { cartItems ->
//            cartAdapter.updateCartItems(cartItems) // Update the adapter with new cart items
//        }
//
//        // Load the user's cart
//        auth.currentUser?.uid?.let { userId ->
//            viewModel.loadUserCart(userId) // Load cart items for the logged-in user
//        }

        binding.addressAdd.setOnClickListener {
            startActivity(Intent(this, AddressActivity::class.java))
        }
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
