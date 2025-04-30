package com.example.afinal.data

import androidx.room.Entity

@Entity(tableName = "steps", primaryKeys = ["username", "date"])
data class Steps(
    val username: String,
    val date: String,
    val steps: Int
)