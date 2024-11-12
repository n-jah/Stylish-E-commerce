package com.example.stylish.model.home

data class Brand(
    val brandName: String = "",
    val imgIcon: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this("", "")
}
