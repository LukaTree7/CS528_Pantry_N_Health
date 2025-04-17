package com.example.afinal.data

import java.security.MessageDigest
import java.util.Base64

class LoginRepository(private val userDao: LoginDao) {

    // Simple password hashing (for better security, use a proper library like BCrypt)
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }

    suspend fun registerUser(username: String, password: String, email: String?): Boolean {
        // Check if username already exists
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser != null) {
            return false // Username already taken
        }

        // Hash the password
        val hashedPassword = hashPassword(password)

        // Create and insert new user
        val user = Login(
            username = username,
            pwd = hashedPassword
        )

        userDao.registerUser(user)
        return true
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        // Hash the password
        val hashedPassword = hashPassword(password)

        // Check credentials
        val count = userDao.validateCredentials(username, hashedPassword)
        return count > 0
    }

    suspend fun getUserByUsername(username: String): Login? {
        return userDao.getUserByUsername(username)
    }
}