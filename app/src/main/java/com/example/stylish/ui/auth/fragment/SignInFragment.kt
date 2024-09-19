package com.example.stylish.ui.auth.fragment

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.ViewModel.AuthViewModel
import com.example.stylish.databinding.ActivityLoginBinding
import com.example.stylish.repository.AuthRepositoryImpl
import com.example.stylish.repository.AuthRepositoryInterface
import com.example.stylish.repository.AuthViewModelFactory

class SignInFragment : Fragment() {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    private val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityLoginBinding.inflate(inflater, container, false)
        binding.forgetPassId.setOnClickListener {
            (activity as? FragmentChangeListener)?.replaceFragment(ForgotPassowrdFragment())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        viewModel.signInResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess {
                // Handle successful sign-in
                Toast.makeText(requireContext(), "Sign-in successful!", Toast.LENGTH_SHORT).show()
            }.onFailure {
                // Handle sign-in failure
                Toast.makeText(requireContext(), "Sign-in failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })


    }
    fun getUserInput(): Pair<String, String>? {
        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

        return if (password.isNotBlank() && username.isNotBlank()) {
            Pair(username, password)
        } else {
            null
        }
    }

    fun checkRememberMe(): Boolean {
        return binding.rememberMeSwitch.isChecked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
