package com.fintracker.data.repository

import android.util.Log
import com.fintracker.data.model.User
import com.fintracker.data.model.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tag = "AuthRepository"

    val currentUser: FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("User not found")
        Result.success(firebaseUser.toAppUser())
    } catch (e: Exception) {
        Log.e(tag, "signIn error: ${e.message}")
        Result.failure(e)
    }

    suspend fun signUp(email: String, password: String): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("User not found")
        val user = firebaseUser.toAppUser()
        createDefaultCategories(user.uid)
        Result.success(user)
    } catch (e: Exception) {
        Log.e(tag, "signUp error: ${e.message}")
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun createDefaultCategories(userId: String) {
        try {
            val defaultCategories = listOf(
                // === РАСХОДЫ ===
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Продукты", type = "expense", icon = "restaurant", color = "#FF5722", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Транспорт", type = "expense", icon = "directions_car", color = "#2196F3", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Кафе и рестораны", type = "expense", icon = "local_cafe", color = "#795548", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Развлечения", type = "expense", icon = "movie", color = "#9C27B0", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Здоровье", type = "expense", icon = "health_and_safety", color = "#F44336", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Одежда", type = "expense", icon = "checkroom", color = "#3F51B5", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Коммунальные услуги", type = "expense", icon = "home", color = "#009688", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Связь и интернет", type = "expense", icon = "wifi", color = "#607D8B", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Аптека", type = "expense", icon = "local_pharmacy", color = "#E91E63", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Образование", type = "expense", icon = "school", color = "#FF9800", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Подарки", type = "expense", icon = "card_giftcard", color = "#FFC107", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Животные", type = "expense", icon = "pets", color = "#795548", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Ремонт", type = "expense", icon = "build", color = "#8BC34A", isDefault = true),
                // === ДОХОДЫ ===
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Зарплата", type = "income", icon = "work", color = "#4CAF50", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Фриланс", type = "income", icon = "computer", color = "#2196F3", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Инвестиции", type = "income", icon = "trending_up", color = "#009688", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Премия", type = "income", icon = "star", color = "#FFC107", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Кэшбэк", type = "income", icon = "monetization_on", color = "#4CAF50", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Подарки", type = "income", icon = "card_giftcard", color = "#E91E63", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Выигрыш", type = "income", icon = "emoji_events", color = "#FF9800", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Аренда", type = "income", icon = "house", color = "#9C27B0", isDefault = true),
                Category(id = UUID.randomUUID().toString(), userId = userId, name = "Дивиденды", type = "income", icon = "pie_chart", color = "#3F51B5", isDefault = true)
            )
            defaultCategories.forEach { category ->
                firestore.collection("categories").document(category.id).set(category).await()
                Log.d(tag, "Category created: ${category.name}")
            }
            Log.d(tag, "All default categories created for user $userId")
        } catch (e: Exception) {
            Log.e(tag, "Error creating default categories: ${e.message}")
        }
    }

    suspend fun ensureDefaultCategories(): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
        // Проверяем, есть ли уже категории
        val snapshot = firestore.collection("categories")
            .whereEqualTo("userId", uid)
            .limit(1)
            .get()
            .await()
        if (snapshot.documents.isEmpty()) {
            createDefaultCategories(uid)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(tag, "ensureDefaultCategories error: ${e.message}")
        Result.failure(e)
    }

    private fun FirebaseUser.toAppUser(): User = User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}