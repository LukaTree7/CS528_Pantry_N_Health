package com.example.afinal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.util.Date

@Dao
interface LoginDao {
    @Insert
    suspend fun insert(account: Login): Long

    @Query("SELECT * FROM accounts WHERE username = :username LIMIT 1")
    suspend fun getAccount(username: String): Login?

    @Query("SELECT * FROM accounts WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInAccount(): Login?

    @Update
    suspend fun update(account: Login)

    @Query("UPDATE accounts SET isLoggedIn = 0")
    suspend fun logoutAll()

    @Query("UPDATE accounts SET isLoggedIn = 1, lastLoginTime = :currentTime WHERE username = :username")
    suspend fun setLoggedIn(username: String, currentTime: Date = Date())

    @Transaction
    suspend fun login(username: String, currentTime: Date = Date()) {
        logoutAll()
        setLoggedIn(username, currentTime)
    }

    @Query("UPDATE accounts SET height = :height, weight = :weight, age = :age, updatedAt = :currentTime WHERE username = :username")
    suspend fun updateProfile(username: String, height: Int?, weight: Int?, age: Int?, currentTime: Date = Date())
}