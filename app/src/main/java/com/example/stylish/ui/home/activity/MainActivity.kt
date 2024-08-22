package com.example.stylish.ui.home.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stylish.R
import com.example.stylish.ViewModel.MainViewModel
import com.example.stylish.adapter.BrandAdapter
import com.example.stylish.adapter.ItemAdapter
import com.example.stylish.databinding.ActivityMainBinding
import com.example.stylish.model.Brand
import com.example.stylish.model.Item

class MainActivity : AppCompatActivity() {

    private val viewModel = MainViewModel()

    lateinit var binding: ActivityMainBinding

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
//new arrival items
        val itemRV = binding.newArrivalRecyclerView

        itemRV.layoutManager = GridLayoutManager(this, 2)
        // itemRV.adapter = ItemAdapter(items, isLoading = true)

// brand items
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
        viewModel.items.observe(this, Observer { items ->
            itemsimg(items)
            binding.newArrivalRecyclerView.adapter = ItemAdapter(items, isLoading = false)




        })
        viewModel.loadItems()

    }
    private fun itemsimg(items: List<Item>){
        binding.newArrivalRecyclerView.adapter = ItemAdapter(items, isLoading = false)

    }
}
