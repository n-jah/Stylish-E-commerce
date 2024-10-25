package com.example.stylish.ui.home.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.stylish.modeldata.CartItemDetail
import com.example.stylish.repository.CartViewModelFactory
import com.example.stylish.repository.FirebaseCartRepositoryImpl
import com.google.firebase.auth.FirebaseAuth

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartViewModel: CartViewModel
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStatusBar()
        auth = FirebaseAuth.getInstance()

        val repository = FirebaseCartRepositoryImpl()
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)

        cartAdapter = CartAdapter(mutableListOf(), onQuantityChange = { cartItem , cartItemKey ->
            // Update the quantity in Firebase
            cartViewModel.updateCartItem(auth.currentUser?.uid ?: "", cartItemKey, cartItem)
            setPrice(cartAdapter.getCartItems())

            // Handle quantity change logic
        }, onRemoveItem = { cartItem  ->
            // Remove item from cart in Firebase using the Firebase key
            cartViewModel.removeItemFromCart(auth.currentUser?.uid ?: "", cartItem.cartItemKey)

            // Remove item from adapter and update the UI
            cartAdapter.removeItem(cartItem)
            setPrice(cartAdapter.getCartItems())


        })


        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }

        // Observe cart items from ViewModel
        cartViewModel.cartItems.observe(this) { cartItems ->
            cartAdapter.updateCartItems(cartItems.toMutableList()) // Ensure it's mutable
            setPrice(cartItems)
            handleEmptyView(cartItems)  // Check if the list is empty and handle the UI

        }
        binding.backButton.setOnClickListener {
            finish()



        }

        // Load the user's cart
        auth.currentUser?.uid?.let { userId ->
            cartViewModel.loadUserCart(userId)
        }

        binding.addressAdd.setOnClickListener {
            startActivity(Intent(this, AddressActivity::class.java))
        }
    }

    private fun setPrice(cartItems: List<CartItemDetail>) {
        // Change from List<CartItemDetail>? to List<CartItemDetail>
        if (cartItems.isNotEmpty()) {
            var verAllPrice: Float = cartViewModel.getTotalPrice()
            val itemsCount: Int = cartAdapter.itemCount

            if (verAllPrice > 0 && itemsCount > 0) {

                val deliveryCharge = 10.0f
                val totalPrice = verAllPrice + deliveryCharge

                binding.subtotalPrice.text = "$$verAllPrice"
                binding.deliveryCharge.text = "$$deliveryCharge"
                binding.totalPrice.text = "$$totalPrice"



            } else {
                // If the prices or items are 0
                binding.subtotalPrice.text = getString(R.string.zeroPrecent)
                binding.deliveryCharge.text = getString(R.string.zeroPrecent)
                binding.totalPrice.text = getString(R.string.zeroPrecent)
            }
        } else {
            // Reset price fields if there are no items
            binding.subtotalPrice.text = getString(R.string.zeroPrecent)
            binding.deliveryCharge.text = getString(R.string.zeroPrecent)
            binding.totalPrice.text = getString(R.string.zeroPrecent)
        }
    }
    // Function to handle the visibility of the empty view
    private fun handleEmptyView(cartItems: List<CartItemDetail>) {
        if (cartItems.isEmpty()) {
            binding.cartRecyclerView.visibility = View.GONE
            binding.emptyCartAnimation.visibility = View.VISIBLE  // For Lottie Animation
        } else {
            binding.cartRecyclerView.visibility = View.VISIBLE
            binding.emptyCartAnimation.visibility = View.GONE
        }
    }
    private fun setupStatusBar() {
        window.statusBarColor = Color.TRANSPARENT
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top - 30, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
