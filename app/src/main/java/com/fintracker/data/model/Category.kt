package com.fintracker.data.model

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "",      // "income" или "expense"
    val icon: String = "",      // имя иконки из Material Icons
    val color: String = "",     // цвет в hex
    val budgetLimit: Double? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)