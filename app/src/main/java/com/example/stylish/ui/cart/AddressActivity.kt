package com.example.stylish.ui.cart

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.R
import com.example.stylish.ViewModel.cart.CartViewModel
import com.example.stylish.ViewModel.cart.CartViewModelFactory
import com.example.stylish.databinding.ActivityAddressBinding
import com.example.stylish.model.user.UserAddress
import com.example.stylish.repository.cart.FirebaseCartRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding

    private lateinit var etName: EditText
    private lateinit var etCountry: EditText
    private lateinit var etGovernment: EditText
    private lateinit var etDetailedAddress: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnSaveAddress: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var cartViewModel: CartViewModel
    private val database by lazy { FirebaseDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpViewModel()

        etName = binding.nameEt
        etCountry = binding.countryEt
        etGovernment = binding.governmentEt
        etDetailedAddress = binding.addressEt
        etPhoneNumber = binding.phoneEt
        btnSaveAddress = binding.addAddress


        btnSaveAddress.setOnClickListener {
             addAddressToFireBase()
        }
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    fun addAddressToFireBase() {
        val userId = auth.currentUser?.uid
        // Collect user input and validate
        val name = etName.text.toString().trim()
        val country = etCountry.text.toString().trim()
        val government = etGovernment.text.toString().trim()
        val detailedAddress = etDetailedAddress.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()

        if (name.isEmpty() || country.isEmpty() || government.isEmpty() || detailedAddress.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create UserAddress object with user input data
        val userAddress = UserAddress(name, country, government, detailedAddress, phoneNumber)

        // Save the address to Firebase

        cartViewModel.addAddress(userId ?: "", userAddress)



        finish()


    }


    private fun setUpViewModel(){
        auth = FirebaseAuth.getInstance()
        val repository = FirebaseCartRepositoryImpl()
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
    }

}





