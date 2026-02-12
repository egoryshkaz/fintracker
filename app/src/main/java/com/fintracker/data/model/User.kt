package com.fintracker.data.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String? = null,
    val photoUrl: String? = null
)