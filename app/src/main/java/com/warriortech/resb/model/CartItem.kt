package com.warriortech.resb.model

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int
) {
    val totalPrice: Double
        get() = menuItem.rate * quantity
}