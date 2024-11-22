package com.example.stylish.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.View
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

        // Observe LiveData for orders
        setupObservers()

        // Set up back button click listener
        binding.backButton.setOnClickListener {
            onBackPressed()
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
