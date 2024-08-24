package com.example.stylish.ui.home.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
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
import com.example.stylish.repository.FirebaseItemRepository

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var  itemRV : RecyclerView

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(FirebaseItemRepository())
    }

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

        val brandList = listOf(
            Brand("nike"),
            Brand("adidas"),
            Brand("puma"),
            Brand("zzz"),
            Brand("nagah"),
            Brand("sutra")
        )

        itemsinti()
// items


        // itemRV.adapter = ItemAdapter(items, isLoading = true)

// brands

        val brandRV = binding.brandsRecyclerView
        brandRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize the adapter with `isLoading = true` initially
        val adapter = BrandAdapter(brandList, isLoading = true)
        brandRV.adapter = adapter

        brandRV.postDelayed({
            // After the delay, update the adapter with the real data and `isLoading = false`
            brandRV.adapter = BrandAdapter(brandList, isLoading = false)
//            itemRV.adapter = ItemAdapter(items, isLoading = false)
        }, 500)


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
        viewModel.loadItems()

    }

}
