package com.example.afinal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class Steps(
    @PrimaryKey val username: String,
    val date: String,
    val steps: Int
)
