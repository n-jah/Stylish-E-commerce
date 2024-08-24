package com.example.stylish.ui.home.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stylish.R
import com.example.stylish.ViewModel.MainViewModel
import com.example.stylish.adapter.BrandAdapter
import com.example.stylish.adapter.ItemAdapter
import com.example.stylish.databinding.ActivityMainBinding
import com.example.stylish.model.Brand
import com.example.stylish.model.Item
import com.example.stylish.repository.FirebaseBrandRepositry
import com.example.stylish.repository.FirebaseItemRepository
import com.example.stylish.repository.MianViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var  itemRV : RecyclerView
    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        viewModel = ViewModelProvider(this, MianViewModelFactory(FirebaseItemRepository(),FirebaseBrandRepositry()))
            .get(MainViewModel::class.java)


        brandsinti()

        itemsinti()

// brands

        val brandRV = binding.brandsRecyclerView
        brandRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)



    }

    private fun brandsinti() {
        val brandRV = binding.brandsRecyclerView
        brandRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        brandRV.adapter = BrandAdapter(isLoading = true)
        viewModel.brands.observe(this, Observer { brands ->
            if (brands != null){
                brandRV.adapter = BrandAdapter(brands, isLoading = false)
                }
        })
    }

    private fun itemsinti() {

        itemRV = binding.newArrivalRecyclerView
        itemRV.layoutManager = GridLayoutManager(this, 2)
        itemRV.adapter = ItemAdapter(isLoading = true)

        viewModel.items.observe(this, Observer { items ->
            if (items != null){
                itemRV.adapter = ItemAdapter(items, isLoading = false)

            }

        })


    }

}
