package com.fintracker.data.repository

import com.fintracker.data.model.Budget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class BudgetRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    suspend fun getCurrentMonthBudget(): Budget? = try {
        val uid = userId ?: return null
        val currentMonth = YearMonth.now().format(DateTimeFormatter.ISO_DATE)
        val snapshot = firestore.collection("budgets")
            .whereEqualTo("userId", uid)
            .whereEqualTo("month", currentMonth)
            .limit(1)
            .get()
            .await()
        snapshot.documents.firstOrNull()?.toObject(Budget::class.java)
    } catch (e: Exception) {
        null
    }

    suspend fun setBudget(amount: Double): Result<Unit> = try {
        val uid = userId ?: throw Exception("User not logged in")
        val currentMonth = YearMonth.now().format(DateTimeFormatter.ISO_DATE)
        val budget = Budget(
            userId = uid,
            month = currentMonth,
            amount = amount
        )
        firestore.collection("budgets").add(budget).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}