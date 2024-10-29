package com.example.stylish.utilities

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object UserUtils {

    const val USER_NAME_KEY = "username"
    const val PREFS_NAME = "userPrefs"
    const val PREFS_PROFILE_PIC_URL = "profilePicUrl"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
    fun getProfilePicUrlInSharedPreferences(context: Context):String?{
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(PREFS_PROFILE_PIC_URL,"")
    }

    fun clearUserInfoPreference(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(USER_NAME_KEY).apply()
        sharedPreferences.edit().remove(PREFS_PROFILE_PIC_URL).apply()

    }
}
