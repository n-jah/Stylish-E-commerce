package com.example.stylish.model.auth

data class SignUpResult(
    val success: Boolean,
    val errorMessage: String? = null
    // Add other properties as needed for error handling

)
