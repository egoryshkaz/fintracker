package com.fintracker.data.repository

import com.fintracker.data.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose

class TransactionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    // ========== РЕАЛЬНОЕ ВРЕМЯ: SNAPSHOT LISTENER ==========
    fun getTransactionsRealtime(
        startDate: Long? = null,
        endDate: Long? = null,
        categoryId: String? = null
    ): Flow<Result<List<Transaction>>> = callbackFlow {
        val uid = userId ?: throw Exception("User not logged in")
        var query = firestore.collection("transactions")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)

        startDate?.let {
            query = query.whereGreaterThanOrEqualTo("date", it)
        }
        endDate?.let {
            query = query.whereLessThanOrEqualTo("date", it)
        }
        categoryId?.let {
            query = query.whereEqualTo("categoryId", it)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val transactions = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Transaction::class.java)
            } ?: emptyList()
            trySend(Result.success(transactions))
        }

        awaitClose { registration.remove() }
    }

    // ========== ОСТАЛЬНЫЕ МЕТОДЫ БЕЗ ИЗМЕНЕНИЙ ==========
    suspend fun addTransaction(transaction: Transaction): Result<Unit> = try {
        val userId = userId ?: throw Exception("User not logged in")
        val transactionWithUser = transaction.copy(userId = userId)
        firestore.collection("transactions")
            .document(transactionWithUser.id)
            .set(transactionWithUser)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> = try {
        firestore.collection("transactions")
            .document(transaction.id)
            .set(transaction)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTransaction(transactionId: String): Result<Unit> = try {
        firestore.collection("transactions")
            .document(transactionId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}