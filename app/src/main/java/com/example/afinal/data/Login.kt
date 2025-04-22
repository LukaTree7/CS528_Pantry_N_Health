package com.example.afinal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "accounts")
data class Login(
    @PrimaryKey val username: String,
    val pwd: String,
    val height: Int? = null,
    val weight: Int? = null,
    val age: Int? = null,
    val isLoggedIn: Boolean = false,
    val lastLoginTime: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun toSecureCopy(): Login {
        return this.copy(pwd = "")
    }
}