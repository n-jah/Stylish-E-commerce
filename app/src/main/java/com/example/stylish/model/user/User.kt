package com.example.stylish.model.user
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicUrl: String = "",
    val itemsInCart: List<String> = emptyList(),
    val itemsInWishlist: List<String> = emptyList(),
    val itemsInOrders: List<String> = emptyList(),
    val isAdmin: Boolean = false
) {
    constructor(userId: String, username: String, email: String, profilePicUrl: String, isAdmin: Boolean)
            : this(userId, username, email, profilePicUrl, emptyList(), emptyList(), emptyList(),isAdmin)

}