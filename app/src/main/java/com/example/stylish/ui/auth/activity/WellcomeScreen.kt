package com.example.stylish.ui.auth.activity

import android.content.Intent
import android.graphics.Color
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
import androidx.fragment.app.FragmentTransaction
import com.example.stylish.ui.auth.fragment.FragmentChangeListener
import com.example.stylish.R
import com.example.stylish.databinding.ActivityLoginBinding
import com.example.stylish.ui.auth.fragment.SartFragment
import com.example.stylish.databinding.ActivityWellcomeScreenBinding
import com.example.stylish.databinding.FragmentSartBinding
import com.example.stylish.databinding.FragmentSignUpBinding
import com.example.stylish.ui.auth.fragment.ForgotPassowrdFragment
import com.example.stylish.ui.auth.fragment.NewPassowordFragment
import com.example.stylish.ui.auth.fragment.SignInFragment
import com.example.stylish.ui.auth.fragment.SignUpFragment
import com.example.stylish.ui.auth.fragment.VerificationFragment
import com.example.stylish.ui.home.activity.MainActivity
class WellcomeScreen : AppCompatActivity(), FragmentChangeListener {

    private lateinit var baseButton: Button
    private lateinit var binding: ActivityWellcomeScreenBinding
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var fab: AppCompatImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT

        initVars()

        setContentView(binding.root)

        fragmentContainerView = binding.fragmentContainerView

        initBaseButton()

        // Replace the initial fragment with animations if none is already added
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) == null) {
            replaceFragmentWithAnimations(SartFragment())
        }

        // Set the button text and action based on the current fragment
        updateButtonState(baseButton)
    }

    private fun initBaseButton() {
        baseButton = binding.buttonBase
        baseButton.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            val newFragment = when (currentFragment) {
                is SartFragment -> SignUpFragment()
                is SignUpFragment -> {
                    Toast.makeText(this, "signup", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                is SignInFragment -> {
                    Toast.makeText(this, "login", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                is VerificationFragment -> NewPassowordFragment()
                is ForgotPassowrdFragment -> VerificationFragment()

                else -> {
                            startActivity(Intent(this,MainActivity::class.java))
                            finish()
                        return@setOnClickListener}
            }
            replaceFragmentWithAnimations(newFragment)

        }
    }

    private fun updateButtonState(button: Button) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        button.text = when (currentFragment) {
            is SartFragment -> getString(R.string.create_an_account)
            is SignUpFragment -> getString(R.string.sign_up)
            is SignInFragment -> getString(R.string.sign_in)
            is ForgotPassowrdFragment -> getString(R.string.confirm_email)
            is VerificationFragment -> getString(R.string.confirm_code)
            is NewPassowordFragment -> getString(R.string.confirm_password)
            else -> getString(R.string.create_an_account)
        }
    }

    override fun replaceFragment(fragment: Fragment) {
        replaceFragmentWithAnimations(fragment)
    }

    private fun replaceFragmentWithAnimations(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit,
            R.anim.fragment_pop_enter,
            R.anim.fragment_pop_exit
        )
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        supportFragmentManager.executePendingTransactions()
        updateButtonState(baseButton)
    }

    private fun initVars() {
        binding = ActivityWellcomeScreenBinding.inflate(layoutInflater)

        // FloatingActionButton for back navigation
        fab = binding.fab
        fab.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed()
                updateButtonState(baseButton)
            }
        }
    }
}
