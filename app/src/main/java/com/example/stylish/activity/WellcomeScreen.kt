package com.example.stylish.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.stylish.fragment.FragmentChangeListener
import com.example.stylish.R
import com.example.stylish.fragment.SartFragment
import com.example.stylish.fragment.SignUpFragment
import com.example.stylish.databinding.ActivityWellcomeScreenBinding
import com.example.stylish.databinding.FragmentSartBinding
import com.example.stylish.ui.login.LoginFragment

class WellcomeScreen : AppCompatActivity() , FragmentChangeListener {

    private lateinit var baseButton: Button
    private lateinit var binding: ActivityWellcomeScreenBinding
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var signInText: TextView
    private lateinit var fab: AppCompatImageButton
    private lateinit var fragmentBinding: FragmentSartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWellcomeScreenBinding.inflate(layoutInflater)
        fragmentBinding = FragmentSartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseButton = binding.buttonBase
        fab = binding.fab
        fragmentContainerView = binding.fragmentContainerView
        
        
        signInText = fragmentBinding.signInText

        // Set up the FloatingActionButton to handle back navigation
        fab.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed()
                updateButtonState(baseButton)
            }
        }

        // Replace the initial fragment with animations if none is already added
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) == null) {
            val initialTransaction = supportFragmentManager.beginTransaction()
            initialTransaction.setCustomAnimations(
                R.anim.fragment_enter,  // Enter animation
                R.anim.fragment_exit,   // Exit animation
                R.anim.fragment_pop_enter,  // Pop enter animation
                R.anim.fragment_pop_exit  // Pop exit animation
            )
            initialTransaction.replace(R.id.fragmentContainerView, SartFragment())
            initialTransaction.commit()
        }

        // Set the button text and action based on the current fragment
        updateButtonState(baseButton)

        baseButton.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            val newFragment = if (currentFragment is SartFragment) {
                SignUpFragment()

            } else if (currentFragment is SignUpFragment) {
                // Handle specific action for SignUpFragment if needed
                Toast.makeText(this, "signup", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            } else if (currentFragment is LoginFragment) {

                // Handle specific action for LoginFragment if needed
                Toast.makeText(this, "login", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            } else {
                SartFragment() // Fallback fragment
            }
            // Replace the fragment with animations
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.fragment_enter,  // Enter animation
                R.anim.fragment_exit,   // Exit animation
                R.anim.fragment_pop_enter,  // Pop enter animation
                R.anim.fragment_pop_exit  // Pop exit animation
            )
            transaction.replace(R.id.fragmentContainerView, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()

            // Update button state after transaction is committed
            supportFragmentManager.executePendingTransactions()
            updateButtonState(baseButton)
        }

        // Handle window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.wellcommain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateButtonState(button: Button) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        button.text = when (currentFragment) {
            is SartFragment -> getString(R.string.create_an_account)
            is SignUpFragment -> getString(R.string.sign_up)
            is LoginFragment -> getString(R.string.sign_in)
            else -> getString(R.string.create_an_account) // Provide a default text if needed
        }
    }

    override fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fragment_enter,  // Enter animation
            R.anim.fragment_exit,   // Exit animation
            R.anim.fragment_pop_enter,  // Pop enter animation
            R.anim.fragment_pop_exit  // Pop exit animation
        )
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        // Update button state after transaction is committed
        supportFragmentManager.executePendingTransactions()
        updateButtonState(baseButton)
    }
}
