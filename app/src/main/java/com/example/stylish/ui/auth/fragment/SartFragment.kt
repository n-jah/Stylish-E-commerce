package com.example.stylish.ui.auth.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.ViewModel.auth.AuthViewModel
import com.example.stylish.databinding.FragmentSartBinding
import com.example.stylish.repository.auth.AuthRepositoryImpl
import com.example.stylish.repository.auth.AuthRepositoryInterface
import com.example.stylish.ViewModel.auth.AuthViewModelFactory
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.PREFS_NAME
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.REMEMBER_ME_KEY
import com.example.stylish.ui.home.activity.MainActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SartFragment : Fragment() {

    private var fragmentChangeListener: FragmentChangeListener? = null
    private lateinit var binding: FragmentSartBinding
    private lateinit var callbackManager: CallbackManager
    private lateinit var authViewModel: AuthViewModel
    private val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()

    companion object {
        const val RC_SIGN_IN = 123  // For Google Sign-In
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentChangeListener = context
        } else {
            throw RuntimeException("$context must implement FragmentChangeListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSartBinding.inflate(inflater, container, false)
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        callbackManager = CallbackManager.Factory.create()
        setupFacebookLogin()
        setupTwitterLogin()

        binding.btnGoogle.setOnClickListener {
            authViewModel.initiateGoogleSignIn(this, RC_SIGN_IN)
        }
        // Access views using the binding object
        val signInText = binding.signInText
        signInText.setOnClickListener {
            fragmentChangeListener?.replaceFragment(SignInFragment())
        }
        // Observe Google, Facebook, and Twitter sign-in results
        observeSignInResults()

        return binding.root
    }

    private fun observeSignInResults() {
        authViewModel.googleSignInResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { handleSignInSuccess() }.onFailure { handleSignInFailure(it.message) }
        })

        authViewModel.facebookSignInResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { handleSignInSuccess() }.onFailure { handleSignInFailure(it.message) }
        })

        authViewModel.twitterSignInResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess { handleSignInSuccess() }.onFailure { handleSignInFailure(it.message) }
        })
    }

    private fun setupFacebookLogin() {
        binding.btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email", "public_profile")
            )
        }

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val token = loginResult.accessToken.token
                    authViewModel.handleFacebookSignInResult(token)  // ViewModel handles token
                }

                override fun onCancel() {
                    Toast.makeText(context, "Facebook login canceled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(context, "Facebook login failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupTwitterLogin() {
        binding.btnTwitter.setOnClickListener {
            val provider = OAuthProvider.newBuilder("twitter.com")

            val pendingResultTask = FirebaseAuth.getInstance().pendingAuthResult
            if (pendingResultTask != null) {
                pendingResultTask.addOnSuccessListener { authResult ->
                    authViewModel.handleTwitterSignInResult(authResult)  // Use ViewModel
                }.addOnFailureListener {
                    handleSignInFailure(it.message)
                }
            } else {
                FirebaseAuth.getInstance()
                    .startActivityForSignInWithProvider(requireActivity(), provider.build())
                    .addOnSuccessListener { authResult ->
                        authViewModel.handleTwitterSignInResult(authResult)  // Use ViewModel
                    }
                    .addOnFailureListener { error ->
                        handleError(error)
                    }
            }
        }
    }

    private fun handleError(error: Exception) {
        if (error is FirebaseAuthUserCollisionException) {
            Toast.makeText(requireContext(), "Account already linked with another provider", Toast.LENGTH_SHORT).show()
        } else {
            handleSignInFailure(error.message)
        }
    }

    private fun handleSignInSuccess() {
        sharedPrefSwitcher(true)
        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    private fun handleSignInFailure(errorMessage: String?) {
        Toast.makeText(requireContext(), "Sign-In failed: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            authViewModel.handleGoogleSignInResult(data)
        }
    }

    private fun sharedPrefSwitcher(switchCase: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(REMEMBER_ME_KEY, switchCase)
        editor.apply()
    }

    override fun onDetach() {
        super.onDetach()
        fragmentChangeListener = null
    }
}

interface FragmentChangeListener {
    fun replaceFragment(fragment: Fragment)
}
