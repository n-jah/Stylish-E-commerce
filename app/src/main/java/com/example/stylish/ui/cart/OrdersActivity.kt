package com.example.stylish.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.ViewModel.cart.CartViewModel
import com.example.stylish.ViewModel.cart.CartViewModelFactory
import com.example.stylish.adapter.OrdersAdapter
import com.example.stylish.databinding.ActivityOrdersBinding
import com.example.stylish.model.cart.Order
import com.example.stylish.repository.cart.FirebaseCartRepositoryImpl
import com.example.stylish.utilities.UiState
import com.google.android.material.snackbar.Snackbar

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var viewModel: CartViewModel
    private lateinit var orders:List<Order>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        supportActionBar?.hide()

        orders = emptyList()
        // Inflate the layout
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        setupViewModel()

        // Set up RecyclerView
        setupRecyclerView()
        // Set up loader
        setupProgressbar()

        // Observe LiveData for orders
        setupObservers()

        // Set up back button click listener
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

    }

    private fun setupProgressbar() {
        // Observe UI state from ViewModel
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading indicator
                    binding.progressBar.visibility = View.VISIBLE
                    binding.ordersRecyclerView.visibility = View.GONE
                    binding.emptyoredersAnimation.visibility = View.GONE

                }
                is UiState.Success -> {
                    // Hide loading indicator and update UI with data
                    binding.progressBar.visibility = View.GONE
                    binding.emptyoredersAnimation.visibility = View.GONE
                    setupAnimation(viewModel.orders.value ?: emptyList())
                }
                is UiState.Error -> {
                    // Hide loading indicator and show error message
                    binding.progressBar.visibility = View.GONE
                    binding.emptyoredersAnimation.visibility = View.VISIBLE
                    binding.ordersRecyclerView.visibility = View.GONE

                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()

                }
            }
        }
    }

    private fun setupAnimation(orderss: List<Order>) {

        if (orderss.isEmpty()) {
            binding.emptyoredersAnimation.visibility = View.VISIBLE
            binding.ordersRecyclerView.visibility = View.GONE
        } else {
            binding.emptyoredersAnimation.visibility = View.GONE
            binding.ordersRecyclerView.visibility = View.VISIBLE
        }

    }

    private fun setupViewModel() {
        val repository = FirebaseCartRepositoryImpl()
        val factory = CartViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
        viewModel.getOrders() // Fetch orders from Firebase
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(mutableListOf())
        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
        }
    }

    private fun setupObservers() {
        viewModel.orders.observe(this) { orders ->
            if (orders.isNotEmpty()) {
                Log.d("OrdersActivity", "Orders retrieved: $orders")
                ordersAdapter.updateOrders(orders)
            } else {
                setupAnimation(orders)
                Log.d("OrdersActivity", "No orders found")
            }
        }
    }
}
