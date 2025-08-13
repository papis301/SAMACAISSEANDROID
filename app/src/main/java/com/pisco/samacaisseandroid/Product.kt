package com.pisco.samacaisseandroid

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val quantity: Double,
    val dateAdded: String,
    val imageUri: String? = null,
    val unit: String
)
