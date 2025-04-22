package com.example.afinal

import com.example.afinal.data.Login
import com.example.afinal.data.LoginDao
import java.util.Date

class LoginRepository(private val loginDao: LoginDao) {

    suspend fun getAccount(username: String): Login? {
        return loginDao.getAccount(username)?.toSecureCopy()
    }

    suspend fun getLoggedInAccount(): Login? {
        return loginDao.getLoggedInAccount()?.toSecureCopy()
    }

    suspend fun registerUser(
        username: String,
        password: String,
        height: Int? = null,
        weight: Int? = null,
        age: Int? = null
    ): Boolean {
        return try {
            val account = Login(
                username = username,
                pwd = password,
                height = height,
                weight = weight,
                age = age,
                isLoggedIn = false,
                lastLoginTime = null,
                createdAt = Date(),
                updatedAt = Date()
            )
            loginDao.insert(account) > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        val account = loginDao.getAccount(username)
        return if (account != null && account.pwd == password) {
            loginDao.login(username)
            true
        } else {
            false
        }
    }

    suspend fun logout() {
        loginDao.logoutAll()
    }

    suspend fun updateProfile(
        username: String,
        height: Int?,
        weight: Int?,
        age: Int?
    ): Boolean {
        return try {
            loginDao.updateProfile(username, height, weight, age, Date())
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updatePassword(username: String, newPassword: String): Boolean {
        return try {
            val account = loginDao.getAccount(username)
            account?.let {
                val updated = it.copy(pwd = newPassword, updatedAt = Date())
                loginDao.update(updated)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}