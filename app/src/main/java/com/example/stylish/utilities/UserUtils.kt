package com.example.stylish.utilities

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object UserUtils {

    const val USER_NAME_KEY = "username"
    const val PREFS_NAME = "userPrefs"
    const val PREFS_PROFILE_PIC_URL = "profilePicUrl"
    const val PREFS_PROFILE_Email = "email"


     val auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun getFirstName(fullName: String): String {
        return fullName.split(" ").firstOrNull() ?: ""
    }

    fun getLastName(fullName: String): String {
        return fullName.split(" ").lastOrNull() ?: ""
    }

    fun getDisplayName(): String? {
        return auth.currentUser?.displayName // Returns null if currentUser is null
    }

    fun getFullName(firstName: String, lastName: String): String {
        return "$firstName $lastName"
    }
    fun getUserNameFromSharedPreferences(context :Context):String?{
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(USER_NAME_KEY,"")
    }
    fun saveUserNameInSharedPreferences(context: Context, username: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(USER_NAME_KEY, username).apply()
    }

    fun saveProfilePicUrlInSharedPreferences(context: Context, profilePicUrl: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PREFS_PROFILE_PIC_URL, profilePicUrl).apply()

    }
    fun saveEmailInSharedPreferences(context: Context, email: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PREFS_PROFILE_Email, email).apply()
    }

    fun getEmailFromSharedPreferences(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_PROFILE_Email,"")
    }
    fun getProfilePicUrlInSharedPreferences(context: Context):String?{
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_PROFILE_PIC_URL,"")
    }
    // Helper function to save theme preference
    fun saveThemePreference(context: Context,isDarkMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }
    fun getThemePreference(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isDarkMode", false)
    }


    fun clearUserInfoPreference(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(USER_NAME_KEY).apply()
        sharedPreferences.edit().remove(PREFS_PROFILE_PIC_URL).apply()

    }
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }


}
