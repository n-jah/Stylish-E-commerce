package com.example.stylish.repository.auth

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.stylish.MyApp
import com.example.stylish.R
import com.example.stylish.model.user.User
import com.example.stylish.utilities.UserUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepositoryInterface {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storageRef = FirebaseStorage.getInstance().reference
    private val databaseRef = FirebaseDatabase.getInstance().reference

    override suspend fun signUp(email: String, password: String, username: String): Result<String> {
        return try {
            val authResult = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await()

            val userId = authResult.user?.uid ?: throw Exception("User ID is null")
            val userMap = mapOf("username" to username, "email" to email)

            FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .setValue(userMap)
                .await()

            Result.success("Sign-up successful")
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthUserCollisionException -> Result.failure(Exception("Email already in use"))
                is FirebaseAuthWeakPasswordException -> Result.failure(Exception("Weak password"))
                else -> Result.failure(e)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await()
            Result.success("Sign-in successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Boolean> {
        return try {
            FirebaseAuth.getInstance().signOut()
            Result.success(true)  // Return success result
        } catch (e: Exception) {
            Result.failure(e)  // Return failure result if any exception occurs
        }
    }

    override suspend fun firebaseAuthWithGoogle(idToken: String): Result<String> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success("Google Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(MyApp.instance.getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(MyApp.instance, gso)  // Ensure App.instance is your Application context
    }

    override suspend fun firebaseAuthWithFacebook(token: String): Result<String> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            auth.signInWithCredential(credential).await()
            Result.success("Facebook Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithTwitter(authResult: AuthResult): Result<String> {
        return try {
            auth.signInWithCredential(authResult.credential!!).await()  // Ensure you handle the credential appropriately
            Result.success("Twitter Sign-In successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restorePasswordWithEmail(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success("Password reset email sent")
        } catch (e: Exception) {
            Result.failure(e)
        }
     }


    override suspend fun uploadImgProfileReturnUrl(
        imageUri: String,
        loading: (Boolean) -> Unit
    ): Result<String> {
        return try {
            val userId = UserUtils.getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
            val fileRef = storageRef.child("profile_images/$userId/profile.jpg")
            loading(true)
            fileRef.putFile(imageUri.toUri()).await()
            val downloadUrl = fileRef.downloadUrl.await()
            loading(false)
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            loading(false)
            Result.failure(e)
        }

    }
    override suspend fun getUserData(onLoading: (Boolean) -> Unit): Result<User> {
        return try {
            onLoading(true) // Start loading

            // Step 1: Check Local Storage (SharedPreferences)
            val userName = UserUtils.getUserNameFromSharedPreferences(MyApp.instance.applicationContext)
            val profilePicUrl = UserUtils.getProfilePicUrlInSharedPreferences(MyApp.instance.applicationContext)
            val email = UserUtils.getEmailFromSharedPreferences(MyApp.instance.applicationContext)
            // If data is available in local storage
            if (!userName.isNullOrEmpty() && !profilePicUrl.isNullOrEmpty() && !email.isNullOrEmpty()) {
                onLoading(false) // End loading
                Log.d("AuthRepositoryImpl", "User data from local storage: $userName, $profilePicUrl, $email")
                return Result.success(User(username = userName, profilePicUrl = profilePicUrl, email = email))
            }

            // Step 2: Check Firebase Auth (Basic data)
            val user = UserUtils.auth.currentUser
            if (user != null && user.displayName != null && user.photoUrl != null && user.email != null) {
                // Save to SharedPreferences
                UserUtils.saveUserNameInSharedPreferences(MyApp.instance.applicationContext, user.displayName ?: "")
                UserUtils.saveProfilePicUrlInSharedPreferences(MyApp.instance.applicationContext, user.photoUrl.toString())
                UserUtils.saveEmailInSharedPreferences(MyApp.instance.applicationContext, user.email ?: "")
                onLoading(false) // End loading
                return Result.success(User(username = user.displayName ?: "", profilePicUrl = user.photoUrl.toString(), email = user.email ?: ""))
            }

            // Step 3: Fetch from Firebase Realtime Database (Fallback)
            val userId = user?.uid ?: return Result.failure(Exception("User is not logged in"))
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            val snapshot = userRef.get().await()
            val userNameFromDb = snapshot.child("username").value as? String
            val profilePicUrlFromDb = snapshot.child("profilePicUrl").value as? String
            val emailFromDb = snapshot.child("email").value as? String

            if (userNameFromDb != null && profilePicUrlFromDb != null && emailFromDb != null) {
                // Save to SharedPreferences
                UserUtils.saveUserNameInSharedPreferences(MyApp.instance.applicationContext, userNameFromDb)
                UserUtils.saveProfilePicUrlInSharedPreferences(MyApp.instance.applicationContext, profilePicUrlFromDb)
                UserUtils.saveEmailInSharedPreferences(MyApp.instance.applicationContext, emailFromDb)
                onLoading(false) // End loading
                return Result.success(User(username = userNameFromDb, profilePicUrl = profilePicUrlFromDb, email = emailFromDb))
            }

            onLoading(false) // End loading if no data found
            Result.failure(Exception("User data not found"))
        } catch (e: Exception) {
            onLoading(false) // End loading on error
            Result.failure(e)
        }
    }
    override suspend fun updateProfilePicUrl(imageUrl: String, loading: (Boolean) -> Unit) {
        val userId = UserUtils.getCurrentUserId()?:""
        loading(true)
        Log.d("AuthRepositoryImpl", "updateProfilePicUrl: $imageUrl")
        UserUtils.saveProfilePicUrlInSharedPreferences(MyApp.instance.applicationContext, imageUrl)
        Log.d("AuthRepositoryImpl", "sharedPref${UserUtils.getProfilePicUrlInSharedPreferences(MyApp.instance.applicationContext)}")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(imageUrl))
            .build()
        auth.currentUser!!.updateProfile(profileUpdates).addOnSuccessListener {
            loading(false)
            Log.d("AuthRepositoryImpl", "updateProfilePicUrl: success")
        }.addOnFailureListener {
            loading(false)
        }
        databaseRef.child("users").child(userId).child("profilePicUrl").setValue(imageUrl).addOnSuccessListener {
            loading(false)
            Log.d("AuthRepositoryImpl", "updateProfilePicUrlrRealtime: success")
        }.addOnFailureListener {
            loading(false)
            Log.d("AuthRepositoryImpl", "updateProfilePicUrlrRealtime: failure")
        }
        loading(false)
    }
    override suspend fun updateUserName(userName: String, loading: (Boolean) -> Unit) {
        loading(true)
        UserUtils.saveUserNameInSharedPreferences(MyApp.instance.applicationContext, userName)
        if (auth.currentUser != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build()
            auth.currentUser!!.updateProfile(profileUpdates).addOnSuccessListener {
                loading(false)
            }.addOnFailureListener {
                loading(false)
            }
        }
        val userId = UserUtils.auth.currentUser?.uid
        databaseRef.child("users").child(userId!!).child("username").setValue(userName).addOnSuccessListener {
            loading(false)
        }.addOnFailureListener {
            loading(false)
        }
        loading(false)
    }
}
