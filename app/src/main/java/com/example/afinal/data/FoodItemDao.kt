package com.example.afinal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items ORDER BY expiryDate ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getFoodItemById(id: Long): FoodItem?

    @Query("SELECT * FROM food_items WHERE barcode = :barcode")
    suspend fun getFoodItemByBarcode(barcode: String): FoodItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem): Long

    @Update
    suspend fun updateFoodItem(foodItem: FoodItem)

    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItem)

    @Query("SELECT * FROM food_items")
    suspend fun getAllFoodItemsList(): List<FoodItem>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchFoodItems(searchQuery: String): Flow<List<FoodItem>>
}