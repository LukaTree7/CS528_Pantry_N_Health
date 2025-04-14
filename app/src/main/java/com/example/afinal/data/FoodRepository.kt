package com.example.afinal.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodItemDao: FoodItemDao) {

    val allFoodItems: Flow<List<FoodItem>> = foodItemDao.getAllFoodItems()

    suspend fun getFoodItemByBarcode(barcode: String): FoodItem? {
        return foodItemDao.getFoodItemByBarcode(barcode)
    }

    suspend fun insertFoodItem(foodItem: FoodItem): Long {
        return foodItemDao.insertFoodItem(foodItem)
    }

    suspend fun updateFoodItem(foodItem: FoodItem) {
        foodItemDao.updateFoodItem(foodItem)
    }

    suspend fun deleteFoodItem(foodItem: FoodItem) {
        foodItemDao.deleteFoodItem(foodItem)
    }

    fun searchFoodItems(query: String): Flow<List<FoodItem>> {
        return foodItemDao.searchFoodItems(query)
    }
}