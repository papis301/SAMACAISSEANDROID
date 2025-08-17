package com.pisco.samacaisseandroid

data class UserSession(
    val username: String,
    val loginTime: String,
    val logoutTime: String?
)
