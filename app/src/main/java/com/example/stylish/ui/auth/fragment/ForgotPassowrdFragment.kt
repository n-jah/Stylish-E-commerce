package com.example.stylish.ui.auth.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.ViewModel.auth.AuthViewModel
import com.example.stylish.databinding.FragmentForgotPassowrdBinding
import com.example.stylish.repository.auth.AuthRepositoryImpl
import com.example.stylish.repository.auth.AuthRepositoryInterface
import com.example.stylish.ViewModel.auth.AuthViewModelFactory

class ForgotPassowrdFragment : Fragment() {



    private var _binding: FragmentForgotPassowrdBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    private val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View
    {
        _binding = FragmentForgotPassowrdBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)


    }

    fun getEmailOfUser(): String?{
        val email= binding.emailInput.text.toString()

        return if (email.isNotBlank() ) {
            email
        } else {
            null
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}