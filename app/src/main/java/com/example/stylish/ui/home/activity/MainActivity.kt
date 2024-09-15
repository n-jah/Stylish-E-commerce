package com.example.stylish.ui.home.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.example.stylish.repository.FirebaseBrandRepositry
import com.example.stylish.repository.FirebaseItemRepository
import com.example.stylish.repository.MainViewModelFactory
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var  itemRV : RecyclerView
    private lateinit var viewModel: MainViewModel

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_drawer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this, MainViewModelFactory(FirebaseItemRepository(),FirebaseBrandRepositry()))
            .get(MainViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        //the drawer things
        navInti()


        brandsinti()

        itemsinti()


        binding.floatCartButton.setOnClickListener{
            startActivity(Intent(this, CartActivity::class.java))
        }

// brands

        val brandRV = binding.brandsRecyclerView
        brandRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)



    }

    private fun navInti() {
        drawerLayout = binding.mainDrawer
        val navigationView: NavigationView = binding.navView

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.drawer_open, R.string.drawer_close
        )
        binding.floatingSlideIcon.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

        drawerToggle.syncState()

        // Inflate the header view
        val headerView = navigationView.getHeaderView(0)

        // Access the SwitchCompat from the header view
        val switchView = headerView.findViewById<SwitchCompat>(R.id.nav_switch)

        switchView?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Dark mode enabled
                Toast.makeText(this, "Dark mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                // Dark mode disabled
                Toast.makeText(this, "Dark mode disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_account_info -> {
                    // Open Account Information
                }
                // Handle other menu items
            }
            true
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle to handle the toggle button
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)    }
}
