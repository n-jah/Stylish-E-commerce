package com.example.stylish.ui.home.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.ViewModel.auth.AuthViewModel
import com.example.stylish.ViewModel.auth.AuthViewModelFactory
import com.example.stylish.ViewModel.cart.CartViewModel
import com.example.stylish.ViewModel.cart.CartViewModelFactory
import com.example.stylish.ViewModel.home.MainViewModel
import com.example.stylish.ViewModel.home.MainViewModelFactory
import com.example.stylish.databinding.ActivityMainBinding
import com.example.stylish.repository.auth.AuthRepositoryImpl
import com.example.stylish.repository.cart.CartRepository
import com.example.stylish.repository.cart.FirebaseCartRepositoryImpl
import com.example.stylish.repository.home.FirebaseBrandRepositry
import com.example.stylish.repository.home.FirebaseItemRepository
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.PREFS_NAME
import com.example.stylish.ui.auth.activity.WellcomeScreen
import com.example.stylish.ui.cart.CartActivity
import com.example.stylish.ui.cart.OrdersActivity
import com.example.stylish.ui.home.fragment.HomeFragment
import com.example.stylish.ui.home.fragment.WishlistFragment
import com.example.stylish.utilities.UserUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    // Binding and ViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var cartViewModel : CartViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set initial fragment (HomeFragment)
        if (savedInstanceState == null) {
            setCurrentFragment(HomeFragment(), "HOME_FRAGMENT", isLeftAnimation = false)
        }
        initViewModels()  // Initialize ViewModels
        setupUI()         // Setup UI Components
        initObservers()   // Initialize LiveData observers
        setupUserData()
    }

    private fun setupUserData() {
        viewModel.userDataLiveData.observe(this, Observer { user ->

            user?.let {

                updateProfileInfoUI(it.username, it.profilePicUrl)
            }

        })

    }

    private fun initObservers() {
        // Observe logout result
        authViewModel.signOutResult.observe(this, Observer { result ->
            result.onSuccess {
                clearRememberMePreference()
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                navigateToLoginScreen()
            }.onFailure {
                Toast.makeText(this, "Failed to log out", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Update the UI with user information
    private fun updateProfileInfoUI(userName: String, profilePicUrl: String) {
        val headerView = binding.navView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.nav_header_username)
        val profileImageView = headerView.findViewById<ImageView>(R.id.nav_header_image)
        nameTextView.text = userName
        Glide.with(this)
            .load(profilePicUrl)
            .apply(RequestOptions().transform(CenterInside(), CircleCrop()))
            .placeholder(R.drawable.profile_placeholder)
            .into(profileImageView)
    }

    // Helper function to set the current fragment with animations
    private fun setCurrentFragment(fragment: Fragment, tag: String, isLeftAnimation: Boolean) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        // Avoid reloading the same fragment
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return
        }
        // Set different animations based on the direction of the navigation
        val transaction = supportFragmentManager.beginTransaction()
        if (!isLeftAnimation) {
            transaction.setCustomAnimations(
                R.anim.fragment_pop_enter, // Enter from left
                R.anim.fragment_pop_exit,  // Exit to right
                R.anim.fragment_enter,     // Enter from right (for back stack)
                R.anim.fragment_exit       // Exit to left (for back stack)
            )
        } else {
            transaction.setCustomAnimations(
                R.anim.fragment_enter,     // Enter from right
                R.anim.fragment_exit,      // Exit to left
                R.anim.fragment_pop_enter, // Enter from left (for back stack)
                R.anim.fragment_pop_exit   // Exit to right (for back stack)
            )
        }
        // Replace fragment
        transaction.replace(R.id.fragment_container, fragment, tag)
            .disallowAddToBackStack()
            .commit()
    }

    // Initialize ViewModels
    private fun initViewModels() {
        // Auth ViewModel
        val authFactory = AuthViewModelFactory(AuthRepositoryImpl())
        authViewModel = ViewModelProvider(this, authFactory)[AuthViewModel::class.java]
        // Main ViewModel
        val mainFactory = MainViewModelFactory(
            FirebaseItemRepository(),
            FirebaseBrandRepositry(),
            AuthRepositoryImpl()
        )
        //cart ViewModel
        val cartFactory = CartViewModelFactory(FirebaseCartRepositoryImpl())
        cartViewModel = ViewModelProvider(this, cartFactory)[CartViewModel::class.java]

        viewModel = ViewModelProvider(this, mainFactory)[MainViewModel::class.java]
        auth = FirebaseAuth.getInstance()
    }

    // Set up UI components
    private fun setupUI() {
        setupStatusBar()
        setupDrawer()        // Initialize the drawer navigation
        setupBottomNav()     // Initialize the bottom navigation


        // Cart floating button click
        binding.floatCartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        // Logout button
        binding.logoutLayout.setOnClickListener {
            authViewModel.signOutUser()
        }
    }

    // Set up the bottom navigation
    private fun setupBottomNav() {
        val homeItem = binding.navHome
        val wishlistItem = binding.navWishlist

        // Adjust the padding to account for system insets (especially for devices using gestures)
        ViewCompat.setOnApplyWindowInsetsListener(binding.navView) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply bottom insets to avoid overlap with system navigation bar
            binding.navView.setPadding(
                binding.navView.paddingLeft,
                binding.navView.paddingTop,
                binding.navView.paddingRight,
                systemBarsInsets.bottom // Adds padding to avoid overlap
            )
            insets
        }

        // Home Navigation
        homeItem.setOnClickListener {
            showTextAndHideIcon(homeItem, R.id.home_icon, R.id.home_text)
            hideTextAndShowIcon(wishlistItem, R.id.wishlist_icon, R.id.wishlist_text)
            // Set the fragment to HomeFragment
            setCurrentFragment(HomeFragment(), "HOME_FRAGMENT", isLeftAnimation = false)
        }

        // Wishlist Navigation
        wishlistItem.setOnClickListener {
            showTextAndHideIcon(wishlistItem, R.id.wishlist_icon, R.id.wishlist_text)
            hideTextAndShowIcon(homeItem, R.id.home_icon, R.id.home_text)
            // Set the fragment to WishlistFragment
            setCurrentFragment(WishlistFragment(), "WISHLIST_FRAGMENT", isLeftAnimation = true)
        }
    }

    // Helper functions to show and hide icons and texts
    private fun showTextAndHideIcon(item: FrameLayout, iconId: Int, textId: Int) {
        val icon = item.findViewById<ImageView>(iconId)
        val text = item.findViewById<TextView>(textId)

        icon.animate().alpha(0f).setDuration(200).withEndAction {
            icon.visibility = View.GONE
            text.alpha = 0f
            text.visibility = View.VISIBLE
            text.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    // Helper functions to show and hide icons and texts
    private fun hideTextAndShowIcon(item: FrameLayout, iconId: Int, textId: Int) {
        val icon = item.findViewById<ImageView>(iconId)
        val text = item.findViewById<TextView>(textId)

        text.animate().alpha(0f).setDuration(200).withEndAction {
            text.visibility = View.GONE
            icon.alpha = 0f
            icon.visibility = View.VISIBLE
            icon.animate().alpha(1f).setDuration(200).start()
        }.start()
    }

    // Set up the status bar
    private fun setupStatusBar() {
        window.statusBarColor = Color.TRANSPARENT
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top - 30, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Set up the drawer navigation
    private fun setupDrawer() {
        drawerLayout = binding.mainHome
        val navigationView: NavigationView = binding.navView

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.drawer_open, R.string.drawer_close
        )
        drawerToggle.syncState()
        // Drawer toggle button
        binding.floatingSlideIcon.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }
        // Setup navigation view header and dark mode switch
        val headerView = navigationView.getHeaderView(0)

        val switchView = headerView.findViewById<SwitchCompat>(R.id.nav_switch)
        val tv_orders_count = headerView.findViewById<TextView>(R.id.tv_orders_count_onhome)
        headerView.findViewById<LinearLayout>(R.id.tv_orders_count).setOnClickListener {
            startActivity(Intent(this,OrdersActivity::class.java))
        }
        cartViewModel.getOrders()
        cartViewModel.orders.observe(this, Observer { orders ->
            if (orders.isNotEmpty()) {
                val count = orders.size.toString()
                "$count Orders".also { tv_orders_count.text = it }
            } else {
                "0 Orders".also { tv_orders_count.text = it }
            }
        })
// Initialize switch state based on current them
        val checkTheme = UserUtils.getThemePreference(this)
        if (checkTheme) {
            switchView.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

         } else {
            switchView.isChecked = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        switchView.setOnCheckedChangeListener { _, isChecked ->
            // Determine theme mode
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            // Save preference
            UserUtils.saveThemePreference(this, isChecked)
            // Apply the selected theme
            AppCompatDelegate.setDefaultNightMode(mode)
            // Recreate activity to apply the theme
            recreate()

        }


        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationMenu(menuItem)
            true
        }
    }

    // Handle navigation menu items
    private fun handleNavigationMenu(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_account_info -> {
                Toast.makeText(this, "Account Info", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,AccountInfoActivity::class.java))
            }
            R.id.nav_orders -> {
                startActivity(Intent(this,OrdersActivity::class.java))
            }
            R.id.cart_item -> {
                startActivity(Intent(this,CartActivity::class.java))

            }
        }
    }

    // Navigate to the login screen
    private fun navigateToLoginScreen() {
        val intent = Intent(this, WellcomeScreen::class.java)
        startActivity(intent)
        finish()
    }

    // Clear the remember me preference
    private fun clearRememberMePreference() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            true

        } else super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()



    }


}


