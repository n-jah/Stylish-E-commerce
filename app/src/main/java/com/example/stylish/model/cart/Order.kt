package com.example.stylish.model.cart

import com.example.stylish.model.user.UserAddress

data class Order(
    val orderItems: List<CartItemDetail> = emptyList(),  // Default to an empty list
    val userId: String = "",                            // Default to an empty string
    val date: String = "",                              // Default to an empty string
    val totalPrice: String = "",                        // Default to an empty string
    val address: UserAddress = UserAddress(),           // Default to a blank UserAddress object
    val status: String = "ConfirmedByUser"              // Default value
)
