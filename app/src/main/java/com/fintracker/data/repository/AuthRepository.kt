package com.fintracker.data.repository

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
    val currentUser: FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("User not found")
        Result.success(firebaseUser.toAppUser())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signUp(email: String, password: String): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("User not found")
        val user = firebaseUser.toAppUser()
        createDefaultCategories(user.uid)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun createDefaultCategories(userId: String) {
        val defaultCategories = listOf(
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Продукты", type = "expense", icon = "restaurant", color = "#FF5722", isDefault = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Транспорт", type = "expense", icon = "directions_car", color = "#2196F3", isDefault = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Кафе", type = "expense", icon = "local_cafe", color = "#795548", isDefault = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Развлечения", type = "expense", icon = "movie", color = "#9C27B0", isDefault = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Зарплата", type = "income", icon = "work", color = "#4CAF50", isDefault = true),
            Category(id = UUID.randomUUID().toString(), userId = userId, name = "Переводы", type = "income", icon = "account_balance_wallet", color = "#FFC107", isDefault = true)
        )
        defaultCategories.forEach { category ->
            firestore.collection("categories").document(category.id).set(category).await()
        }
    }

    private fun FirebaseUser.toAppUser(): User = User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}