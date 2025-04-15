package com.example.afinal.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String? = null,
    val expiryDate: Date,
    val calories: Int,
    val imageUri: String? = null,
    val dateAdded: Date = Date()
)