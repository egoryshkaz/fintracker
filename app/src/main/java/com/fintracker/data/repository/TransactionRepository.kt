package com.fintracker.data.repository

import com.fintracker.data.model.Transaction
import com.fintracker.data.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TransactionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    // ========== ТРАНЗАКЦИИ ==========
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

    fun getTransactions(
        startDate: Long? = null,
        endDate: Long? = null,
        categoryId: String? = null
    ): Flow<Result<List<Transaction>>> = flow {
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

        val snapshot = query.get().await()
        val transactions = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Transaction::class.java)
        }
        emit(Result.success(transactions))
    }.catch { e ->
        emit(Result.failure(e))
    }

    // ========== КАТЕГОРИИ ==========
    suspend fun addCategory(category: Category): Result<Unit> = try {
        val userId = userId ?: throw Exception("User not logged in")
        val categoryWithUser = category.copy(userId = userId)
        firestore.collection("categories")
            .document(categoryWithUser.id)
            .set(categoryWithUser)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getCategories(type: String? = null): Flow<Result<List<Category>>> = flow {
        val uid = userId ?: throw Exception("User not logged in")
        var query = firestore.collection("categories")
            .whereEqualTo("userId", uid)
        type?.let {
            query = query.whereEqualTo("type", it)
        }
        val snapshot = query.get().await()
        val categories = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Category::class.java)
        }
        emit(Result.success(categories))
    }.catch { e ->
        emit(Result.failure(e))
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = try {
        firestore.collection("categories")
            .document(categoryId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}