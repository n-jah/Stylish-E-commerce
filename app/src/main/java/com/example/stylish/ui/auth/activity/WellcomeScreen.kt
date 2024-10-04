package com.example.stylish.ui.auth.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.ui.auth.fragment.FragmentChangeListener
import com.example.stylish.R
import com.example.stylish.ViewModel.AuthViewModel
import com.example.stylish.ui.auth.fragment.SartFragment
import com.example.stylish.databinding.ActivityWellcomeScreenBinding
import com.example.stylish.repository.AuthRepositoryImpl
import com.example.stylish.repository.AuthRepositoryInterface
import com.example.stylish.repository.AuthViewModelFactory
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.PREFS_NAME
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.REMEMBER_ME_KEY
import com.example.stylish.ui.auth.fragment.ForgotPassowrdFragment
import com.example.stylish.ui.auth.fragment.SignInFragment
import com.example.stylish.ui.auth.fragment.SignUpFragment
import com.example.stylish.ui.home.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class WellcomeScreen : AppCompatActivity(), FragmentChangeListener {

    private lateinit var baseButton: Button
    private lateinit var binding: ActivityWellcomeScreenBinding
    private lateinit var fragmentContainerView: FragmentContainerView
    private lateinit var fab: AppCompatImageButton

    private val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(this, AuthViewModelFactory(authRepository)).get(AuthViewModel::class.java)

    }


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



        authViewModel.signUpResult.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure {
                Toast.makeText(this, "Sign up failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })
        // Observe signInResult LiveData
        authViewModel.signInResult.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "Sign In successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.onFailure {
                Toast.makeText(this, "Sign In failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })

        authViewModel.restPassword.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "Check your email !", Toast.LENGTH_SHORT).show()
                replaceFragmentWithAnimations(SignInFragment())
            }.onFailure {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initBaseButton() {
        baseButton = binding.buttonBase
        baseButton.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            val newFragment = when (currentFragment) {
                is SartFragment -> SignUpFragment()
                is SignUpFragment -> {
                    val fragment = currentFragment
                    val userInput = fragment.getUserInput()
                    if (userInput != null) {
                        val (email, password, username) = userInput
                        authViewModel.signUpUser(email, password, username)
                        sharedPrefSwitcher(fragment.checkToRememberMe())

                        Toast.makeText(this, "Attempting to sign up", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }

                    return@setOnClickListener
                }
                is SignInFragment -> {
                    val fragment = currentFragment
                    val userInput = fragment.getUserInput()
                    if (userInput != null) {
                        val (email, password) = userInput
                        authViewModel.signInUser(email, password)
                        sharedPrefSwitcher(fragment.checkRememberMe())

                        Toast.makeText(this, "Attempting to sign in", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }


                    return@setOnClickListener
                }
                is ForgotPassowrdFragment -> {
                    val fragment = currentFragment
                    val email = fragment.getEmailOfUser()
                    if (email != null) {

                                authViewModel.restorePasswordWithEmail(email)

                    } else {
                        Toast.makeText(this, "Please fill in the email fields", Toast.LENGTH_SHORT).show()
                    }
                    return@setOnClickListener
                }


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
            updateButtonState(baseButton)
            if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed()
                if (supportFragmentManager.backStackEntryCount == 0) {
                    if (baseButton.text == getString(R.string.create_an_account)) {
                        finish()
                    } else {
                        replaceFragmentWithAnimations(SartFragment())
                    }
                }

            }
        }
    }

    private fun sharedPrefSwitcher(switchCase : Boolean){
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(REMEMBER_ME_KEY, switchCase)
        editor.apply()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        updateButtonState(baseButton)
        if (supportFragmentManager.backStackEntryCount == 0) {
            if (baseButton.text == getString(R.string.create_an_account)) {
                finish()
            } else {
                replaceFragmentWithAnimations(SartFragment())
            }
        }
    }


}

