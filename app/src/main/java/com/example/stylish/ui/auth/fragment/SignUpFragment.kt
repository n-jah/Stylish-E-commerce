// SignUpFragment.kt
package com.example.stylish.ui.auth.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.R
import com.example.stylish.ViewModel.AuthViewModel
import com.example.stylish.databinding.FragmentSignUpBinding
import com.example.stylish.repository.AuthRepositoryImpl
import com.example.stylish.repository.AuthRepositoryInterface
import com.example.stylish.repository.AuthViewModelFactory
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.PREFS_NAME
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.REMEMBER_ME_KEY
import com.google.firebase.auth.FirebaseAuth

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var viewModel: AuthViewModel
    private val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Initialize Firebase Auth
        val firebaseAuth = FirebaseAuth.getInstance()

        // Create repository with FirebaseAuth instance
        val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()

        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        viewModel.signUpResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess {
                saveRememberMePreference(checkToRememberMe())
                Toast.makeText(requireContext(), "Sign up successful!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Sign up failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Handle other view logic
    }

    fun getUserInput(): Triple<String, String, String>? {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val username = binding.usernameInput.text.toString()

        return if (email.isNotBlank() && password.isNotBlank() && username.isNotBlank()) {
            Triple(email, password, username)
        } else {
            null
        }
    }

    fun checkToRememberMe(): Boolean {
        return binding.rememberMeSwitch.isChecked

    }

    // Save the "Remember Me" preference to SharedPreferences
    private fun saveRememberMePreference(keepSignedIn: Boolean) {
        val sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(REMEMBER_ME_KEY, keepSignedIn)
        editor.apply()  // Commit the changes
    }
}
