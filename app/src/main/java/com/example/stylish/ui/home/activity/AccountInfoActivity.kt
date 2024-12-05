package com.example.stylish.ui.home.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.stylish.R
import com.example.stylish.ViewModel.home.MainViewModel
import com.example.stylish.ViewModel.home.MainViewModelFactory
import com.example.stylish.repository.auth.AuthRepositoryImpl
import com.example.stylish.repository.home.FirebaseBrandRepositry
import com.example.stylish.repository.home.FirebaseItemRepository
import com.example.stylish.utilities.UserUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AccountInfoActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var editImageButton: ImageButton
    private lateinit var editNameEditText: EditText
    private lateinit var saveButton: ImageView
    private lateinit var loadingIndicator: ProgressBar // Added loading indicator
    private lateinit var mainViewModel: MainViewModel

    private val auth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val storageRef = FirebaseStorage.getInstance().reference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_info)
        enableEdgeToEdge()

        // Initialize views
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        editImageButton = findViewById(R.id.editImageButton)
        editNameEditText = findViewById(R.id.userNameEditText)
        saveButton = findViewById(R.id.saveIcon)
        loadingIndicator = findViewById(R.id.progressBar2)

        initViewModel()
        initObservers()


        // Handle image edit button click
        editImageButton.setOnClickListener { pickImageFromGallery() }

        // Handle Edit Name functionality
        userName.setOnClickListener {
            userName.visibility = View.GONE
            editNameEditText.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            editNameEditText.setText(userName.text.toString())
            editNameEditText.requestFocus()
        }

        saveButton.setOnClickListener {
            val newName = editNameEditText.text.toString()
            if (newName.isNotEmpty() && newName != userName.text.toString()) {
                saveUpdatedUserName(newName)
            }
            editNameEditText.visibility = View.GONE
            saveButton.visibility = View.GONE
            userName.visibility = View.VISIBLE
        }

        // Handle back button click
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener { finish() }
    }

    private fun initObservers() {

        mainViewModel.loading.observe(this) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        mainViewModel.imageURL.observe(this) { url ->
            url?.let {
                updateProfileImage(it)
            }
        }
        mainViewModel.userDataLiveData.observe(this) { user ->
            user?.let {
                Log.d("Info_activity", "user.username" + user.username)
                updateProfileUI(it.username, it.profilePicUrl, it.email)
            }
        }
    }


    private fun initViewModel() {

        val mainFactory = MainViewModelFactory(
            FirebaseItemRepository(),
            FirebaseBrandRepositry(),
            AuthRepositoryImpl()
        )
        mainViewModel = ViewModelProvider(this, mainFactory)[MainViewModel::class.java]
    }



    private fun updateProfileUI(name: String, profilePicUrl: String, email: String) {
        userName.text = name
        userEmail.text = email
        Glide.with(this)
            .load(profilePicUrl)
            .apply(RequestOptions().transform(CenterInside(), CircleCrop()))
            .into(profileImage)
    }

    private fun saveUpdatedUserName(newName: String) {
        val userId = UserUtils.getCurrentUserId()
        if (userId != null) {
            loadingIndicator.visibility = View.VISIBLE
            val userRef = databaseRef.child("users").child(userId)
            val updates = hashMapOf<String, Any>("username" to newName)

            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    UserUtils.saveUserNameInSharedPreferences(this, newName)
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                    )
                    updateProfileUI(
                        newName,
                        UserUtils.getProfilePicUrlInSharedPreferences(this).toString(),
                        userEmail.text.toString()
                    )
                    loadingIndicator.visibility = View.GONE
                    Toast.makeText(this, "Username updated successfully!", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    loadingIndicator.visibility = View.GONE
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Failed to update username. Retry?",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Retry") {
                            saveUpdatedUserName(newName)
                        }
                        .show()
                }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { saveImage(it.toString()) }
            Log.d("Info_activity", "selectedImageUri.toString()" + selectedImageUri.toString())
        }
    }

    private fun saveImage(imageUri: String) {
        mainViewModel.uploadImgProfileReturnUrl(imageUri)
    }

    private fun updateProfileImage(url: String) {
        Glide.with(this)
            .load(url)
            .apply(RequestOptions().transform(CenterInside(), CircleCrop()))
            .into(profileImage)
    }
}