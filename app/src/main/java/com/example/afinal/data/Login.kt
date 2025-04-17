package com.example.afinal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Login(
    @PrimaryKey val username: String,
    val pwd: String
)