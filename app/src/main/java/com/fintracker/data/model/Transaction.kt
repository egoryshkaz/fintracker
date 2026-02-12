package com.fintracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Transaction(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val amount: Double = 0.0,
    val type: String = "",          // "income" / "expense"
    val categoryId: String = "",
    val categoryName: String = "",  // для быстрого отображения
    val description: String = "",
    val date: Timestamp = Timestamp.now(),
    val photoUrl: String? = null,
    val location: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)