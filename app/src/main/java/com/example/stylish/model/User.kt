package com.example.stylish.model
data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val itemsInCart: List<String> = emptyList(),
    val itemsInWishlist: List<String> = emptyList(),
    val itemsInOrders: List<String> = emptyList()



    ){

    constructor(userId: String  ) : this(userId, "", "", "", emptyList(), emptyList(), emptyList()){}
    constructor(userId: String, username: String, email: String, password: String) : this(userId, username, email, password, emptyList(), emptyList(), emptyList()){}
}
