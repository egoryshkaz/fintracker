package com.fintracker.data.model

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val month: String = "",          // формат "yyyy-MM"
    val totalBudget: Double = 0.0,
    val categories: Map<String, Double> = emptyMap() // categoryId -> лимит
)