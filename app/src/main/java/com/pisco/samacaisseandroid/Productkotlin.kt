package com.pisco.samacaisseandroid

class Productkotlin (
        val id: Int,
        val name: String,
        val price: Double,
        val quantity: Double,
        val unit: String,
        val imageUri: String?,   // peut être null si pas d’image
        val dateAdded: String    // format brut "yyyy-MM-dd HH:mm:ss"
    )

