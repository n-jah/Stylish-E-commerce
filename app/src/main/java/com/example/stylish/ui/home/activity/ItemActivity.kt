package com.example.stylish.ui.home.activity

import android.content.Intent
import android.graphics.Color
import android.media.Rating
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.ViewModel.CartViewModel
import com.example.stylish.adapter.ItemImagesAdapter
import com.example.stylish.adapter.SizeAdapter
import com.example.stylish.databinding.ActivityItemBinding
import com.example.stylish.model.CartItem
import com.example.stylish.model.Item
import com.example.stylish.model.Size
import com.example.stylish.repository.CartRepository
import com.example.stylish.repository.CartViewModelFactory
import com.example.stylish.repository.FirebaseCartRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
class ItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemBinding
    private lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartRepository: CartRepository

    private var selectedSize: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Initialize repository (or inject via DI)
        cartRepository = FirebaseCartRepositoryImpl()

        // Create ViewModel with factory
        val viewModelFactory = CartViewModelFactory(cartRepository)
        cartViewModel = ViewModelProvider(this, viewModelFactory).get(CartViewModel::class.java)

        getBundle()

        binding.cartButton.setOnClickListener {
            val userId = auth.currentUser?.uid // Get the user ID safely
            if (userId != null) {
                // Fetch the user's cart
                cartViewModel.fetchUserCart(userId)


            } else {
                // Handle the case when the user is not authenticated
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }

// Observe cart items when the activity is created

        cartViewModel.cartItems.observe(this) { result ->
            result.forEach {
                Log.d("CartViewModel", "Item in cart: $it")
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.addToCartBtn.setOnClickListener {
            // Ensure that a size is selected before adding to the cart
            if (selectedSize == null) {
                Toast.makeText(this, "Please select a size", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = intent.getParcelableExtra<Item>("object")
            if (item != null && auth.currentUser != null) {
                val cartItem = CartItem((item.id ?: 0).toString(), 1, selectedSize!!)  // Use the selected size
                cartViewModel.addItemToCart(auth.currentUser!!.uid, cartItem)
                finish()
                Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun imgsinti(imges: ArrayList<String>) {
        val rv = binding.itemImagesRv
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = ItemImagesAdapter(imges) { selectedImg ->
            Glide.with(this)
                .load(selectedImg)
                .apply(RequestOptions().transforms(CenterInside()))
                .into(findViewById(R.id.itemImage))
        }
    }

    private fun sizeInti(sizes: List<Size>) {
        val rv = binding.itemSizesRv
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv.adapter = SizeAdapter(sizes) { selectedSizeFromAdapter ->
            // Update the selected size when a size is clicked
            selectedSize = selectedSizeFromAdapter
        }
    }

    private fun getBundle() {
        val item = intent.getParcelableExtra<Item>("object")
        backButton = binding.backButton
        backButton.setOnClickListener {
            finish()
        }

        if (item != null) {
            binding.productTitle.text = item.title
            binding.productPrice.text = item.price.toString()
            binding.totalPrice.text = (item.price * 1.15).toFloat().toString()
            binding.ratingNumber.text = item.rating.toString()
            binding.ratingStars.rating = item.rating.toFloat()
            binding.description.text = item.description

            val requestOptions = RequestOptions().transforms(CenterInside())
            Glide.with(this)
                .load(item.imgUrl[0])
                .apply(requestOptions)
                .into(binding.itemImage)

            val sizes: MutableList<Size> = mutableListOf()
            val sizeObjectList = item.size
            for (n in sizeObjectList) {
                sizes.add(Size(n))
            }
            sizeInti(sizes)
            imgsinti(item.imgUrl)

        } else {
            // Handle the case where item is null
            binding.productTitle.text = "Item not found"
            // You might also want to log an error or show a message to the user
        }
    }
}
