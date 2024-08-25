package com.example.stylish.model

data class Brand(
    val brandName: String = "",
    val imgIcon: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "")
}
