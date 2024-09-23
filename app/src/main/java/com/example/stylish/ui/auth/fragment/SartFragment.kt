package com.example.stylish.ui.auth.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.stylish.R
import com.example.stylish.ViewModel.AuthViewModel
import com.example.stylish.databinding.FragmentSartBinding
import com.example.stylish.model.User
import com.example.stylish.repository.AuthRepositoryImpl
import com.example.stylish.repository.AuthRepositoryInterface
import com.example.stylish.repository.AuthViewModelFactory
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.PREFS_NAME
import com.example.stylish.ui.auth.activity.SplashScreen.Companion.REMEMBER_ME_KEY
import com.example.stylish.ui.home.activity.MainActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SartFragment : Fragment() {

    private var fragmentChangeListener: FragmentChangeListener? = null
    private lateinit var binding: FragmentSartBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var authViewModel: AuthViewModel
    private val authRepository: AuthRepositoryInterface = AuthRepositoryImpl()

    companion object {
        const val RC_SIGN_IN = 123
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
    ): View? {
        // Initialize binding here
        binding = FragmentSartBinding.inflate(inflater, container, false)

        // ViewModel initialization
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        // Initialize Google Sign-In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Set up the button listener after initializing the binding
        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Firebase initialization
        auth = FirebaseAuth.getInstance()

        // Observe Google Sign-In result
        authViewModel.googleSignInResult.observe(viewLifecycleOwner, Observer { result ->
            result.onSuccess {
                // Navigate to MainActivity or wherever necessary
                sharedPrefSwitcher(true)
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }.onFailure {
                Toast.makeText(requireContext(), "Google Sign-In failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access views using the binding object
        val signInText = binding.signInText
        signInText.setOnClickListener {
            fragmentChangeListener?.replaceFragment(SignInFragment())
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    // Send the ID token to your ViewModel to handle sign-in with Firebase
                    it.idToken?.let { idToken ->
                        authViewModel.signInWithGoogle(idToken)
                    }


// Get the current user's UID from FirebaseAuth
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val uid = currentUser?.uid

                    if (uid != null) {
                        // Get displayName, email, and profilePicUrl from GoogleSignInAccount
                        val displayName = currentUser.displayName ?: ""
                        val email = currentUser.email ?: ""
                        val profilePicUrl = currentUser.photoUrl?.toString() ?: ""

                        // Create user object to save in Firebase Database
                        val user = User(uid, displayName, email, profilePicUrl)

                        // Write the user data to Firebase Realtime Database
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users").child(uid)
                        usersRef.setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "User data added to Firebase", Toast.LENGTH_SHORT).show()
                                sharedPrefSwitcher(true)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to add user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.d("Firebase_add", "Failed to add user data: ${e.message}")
                            }
                    } else {
                        Toast.makeText(requireContext(), "User ID is null", Toast.LENGTH_SHORT).show()
                    }


                    // Optionally, store this data in the ViewModel or SharedPreferences
//                    authViewModel.setUserDetails(displayName, email, profilePicUrl)
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun sharedPrefSwitcher(switchCase : Boolean){
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