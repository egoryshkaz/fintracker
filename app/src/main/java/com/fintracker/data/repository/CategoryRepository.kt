package com.fintracker.data.repository

import com.fintracker.data.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class CategoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    // ========== РЕАЛЬНОЕ ВРЕМЯ ==========
    fun getCategoriesRealtime(type: String? = null): Flow<Result<List<Category>>> = callbackFlow {
        val uid = userId ?: throw Exception("User not logged in")
        var query = firestore.collection("categories")
            .whereEqualTo("userId", uid)
        type?.let {
            query = query.whereEqualTo("type", it)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val categories = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Category::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(Result.success(categories))
        }

        awaitClose { registration.remove() }
    }

    // ========== ОСТАЛЬНЫЕ МЕТОДЫ ==========
    suspend fun addCategory(category: Category): Result<Unit> = try {
        val uid = userId ?: throw Exception("User not logged in")
        val newCategory = category.copy(
            id = UUID.randomUUID().toString(),
            userId = uid
        )
        firestore.collection("categories")
            .document(newCategory.id)
            .set(newCategory)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateCategory(category: Category): Result<Unit> = try {
        firestore.collection("categories")
            .document(category.id)
            .set(category)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
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