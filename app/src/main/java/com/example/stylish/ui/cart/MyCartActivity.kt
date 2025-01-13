package com.example.stylish.ui.cart

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.R
import com.example.stylish.ViewModel.cart.CartViewModel
import com.example.stylish.ViewModel.cart.CartViewModelFactory
import com.example.stylish.ViewModel.payment.PaymentViewModel
import com.example.stylish.ViewModel.payment.PaymentViewModelFactory
import com.example.stylish.adapter.CartAdapter
import com.example.stylish.databinding.ActivityMyCartBinding
import com.example.stylish.model.cart.CartItemDetail
import com.example.stylish.repository.cart.FirebaseCartRepositoryImpl
import com.example.stylish.utilities.UiState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class MyCartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyCartBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartViewModel: CartViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var paymentSheet: PaymentSheet
    private var address: String? = null
    private var addressFlag: Boolean = false
    private val viewModel: PaymentViewModel by viewModels {
        PaymentViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStatusBar()
        setUpPayment()
        setUpViewModel()
        setupUI()
        setupOvservers()
    }

    private fun setUpPayment() {
        PaymentConfiguration.init(
            this,
            getString(R.string.publish_key)
        ) // Add your publishable key here
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    private fun setupOvservers() {
        // Observe the client secret from ViewModel
        viewModel.clientSecret.observe(this) { clientSecret ->
            presentPaymentSheet(clientSecret)
        }

        viewModel.error.observe(this) { error ->
            // Hide loading indicator and re-enable buttons on error
            loadingIndicator(false)
            disableButtons(false)
            Toast.makeText(this, "hhhh$error", Toast.LENGTH_SHORT).show()
        }

        // Observe cart items from ViewModel
        cartViewModel.cartItems.observe(this) { cartItems ->
            cartAdapter.updateCartItems(cartItems.toMutableList())
            setPrice(cartItems)
            handleEmptyView(cartItems)
        }

        // Observe UI state from ViewModel
        cartViewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading indicator and disable buttons
                    loadingIndicator(true)
                    disableButtons(true)
                    binding.emptyCartAnimation.visibility = View.GONE
                }

                is UiState.Success -> {
                    // Hide loading indicator and re-enable buttons
                    loadingIndicator(false)
                    disableButtons(false)
                }

                is UiState.Error -> {
                    // Hide loading indicator and re-enable buttons
                    loadingIndicator(false)
                    disableButtons(false)
                    binding.emptyCartAnimation.visibility = View.VISIBLE
                    binding.cartRecyclerView.visibility = View.GONE
                    Snackbar.make(binding.root, "hhh{$state.message}", Snackbar.LENGTH_LONG).show()
                    Log.d("MyCartActivity", "Error: ${state.message}")
                }
            }
        }
    }

    private fun loadingIndicator(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun disableButtons(disable: Boolean) {
        binding.bottomButton.isEnabled = !disable
        binding.checkboxpaymentStrip.isEnabled = !disable
        binding.checkboxpayondelivery.isEnabled = !disable
        binding.addressAdd.isEnabled = !disable
        binding.backButton.isEnabled = !disable
    }

    private fun setupUI() {
        listOfItems()
        binding.addressAdd.setOnClickListener {
            startActivity(Intent(this, AddressActivity::class.java))
        }
        // Load the user's cart
        cartViewModel.loadUserCart()
        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyCartActivity)
            adapter = cartAdapter
        }
        binding.backButton.setOnClickListener {
            finish()
        }
        checkOut()
        swtishChack()
        showAddress()
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.getAddresses()
    }

    private fun showAddress() {
        cartViewModel.getAddresses()
        // Observe the LiveData address and update UI
        cartViewModel.address.observe(this) { local_address ->
            // Update the address UI
            address = local_address.last().detailedAddress
            val government = local_address.last().government
            Log.d("AddressActivity", "Address: ${address.toString()}")
            val textOfAddress = binding.addressTv
            // If the address is null or empty, prompt the user to add it
            if (address.isNullOrEmpty() || address!!.isBlank()) {
                addressFlag = false
                textOfAddress.text = "Add Address"
                textOfAddress.setTextColor(Color.parseColor("#b22222"))

                Log.d("AddressActivity", "Address: $address")

                // Handle click to add address
                textOfAddress.setOnClickListener {
                    startActivity(Intent(this, AddressActivity::class.java))
                }
            } else {
                addressFlag = true
                Log.d("AddressActivity", "Address: $address")
                textOfAddress.text = government + "\n" + address
            }
        }
    }

    private fun checkOut() {
        val checkOutButton = binding.bottomButton

        checkOutButton.setOnClickListener {
            val validation =
                (binding.checkboxpaymentStrip.isChecked || binding.checkboxpayondelivery.isChecked) && addressFlag && cartAdapter.getCartItems()
                    .isNotEmpty()
            if (validation) {
                // Show loading indicator and disable buttons
                loadingIndicator(true)
                disableButtons(true)

                if (binding.checkboxpayondelivery.isChecked) {
                    addToOrders()
                }
                if (binding.checkboxpaymentStrip.isChecked) {
                    val amount = ((cartViewModel.getTotalPrice() + 10) * 100)
                    viewModel.createPaymentFlow(amount.toInt())
                }
            } else {
                if (!(binding.checkboxpaymentStrip.isChecked || binding.checkboxpayondelivery.isChecked)) {
                    Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                }
                if (!addressFlag) {
                    Toast.makeText(this, "Please add an address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun swtishChack() {
        val strip = binding.checkboxpaymentStrip
        val payCash = binding.checkboxpayondelivery
        strip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                payCash.isChecked = false
            }
        }
        payCash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                strip.isChecked = false
            }
        }
    }

    private fun listOfItems() {
        cartAdapter = CartAdapter(mutableListOf(), onQuantityChange = { cartItem, cartItemKey ->
            // Update the quantity in Firebase
            if (cartViewModel.checkStock(cartItem.itemId, cartItem.size, cartItem.quantity)) {
                cartViewModel.updateCartItem(cartItemKey, cartItem)
                setPrice(cartAdapter.getCartItems())
            } else {
                Toast.makeText(this, "Out of Stock", Toast.LENGTH_SHORT).show()
            }
        }, onRemoveItem = { cartItem ->
            // Remove item from cart in Firebase using the Firebase key
            cartViewModel.removeItemFromCart(cartItem.cartItemKey)
            // Remove item from adapter and update the UI
            cartAdapter.removeItem(cartItem)
            setPrice(cartAdapter.getCartItems())
        })
    }

    private fun setUpViewModel() {
        auth = FirebaseAuth.getInstance()
        val repository = FirebaseCartRepositoryImpl()
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
        cartViewModel.getAddresses()
    }

    private fun setPrice(cartItems: List<CartItemDetail>) {
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

    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Payment succeeded
                addToOrders()
                Toast.makeText(this, "Payment complete!", Toast.LENGTH_SHORT).show()
            }

            is PaymentSheetResult.Canceled -> {
                // Payment canceled
                loadingIndicator(false)
                disableButtons(false)
                Toast.makeText(this, "Payment canceled!", Toast.LENGTH_SHORT).show()
            }

            is PaymentSheetResult.Failed -> {
                // Payment failed
                loadingIndicator(false)
                disableButtons(false)
                Toast.makeText(this, "Payment failed: ${result.error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addToOrders() {
        cartViewModel.addOrder { success ->
            if (success) {
                cartViewModel.decreaseStockForAllItems(cartAdapter.getCartItems())
                showOrderConfirmedBottomSheet()
            } else {
                // Handle order failure
                loadingIndicator(false)
                disableButtons(false)
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showOrderConfirmedBottomSheet() {
        val bottomSheet = OrderConfirmedBottomSheet.newInstance()
        bottomSheet.show(supportFragmentManager, "OrderConfirmedBottomSheet")
        bottomSheet.setOnDismissListener {
            finish()
        }
    }

    private fun presentPaymentSheet(clientSecret: String) {
        try {
            val configuration = PaymentSheet.Configuration("Stylish")
            paymentSheet.presentWithPaymentIntent(clientSecret, configuration)
        } catch (e: Exception) {
            // Handle payment sheet initialization errors
            loadingIndicator(false)
            disableButtons(false)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}