package com.example.afinal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LoginDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: Login)

    @Query("SELECT * FROM accounts WHERE username = :username")
    suspend fun getUserByUsername(username: String): Login?

    @Query("SELECT COUNT(*) FROM accounts WHERE username = :username AND pwd = :password")
    suspend fun validateCredentials(username: String, password: String): Int
}