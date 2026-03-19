package com.fintracker.data.model

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val month: String = "",          // формат "yyyy-MM"
    val amount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)