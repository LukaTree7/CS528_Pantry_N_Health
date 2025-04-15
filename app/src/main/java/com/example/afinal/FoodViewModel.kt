package com.example.afinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.FoodDatabase
import com.example.afinal.data.FoodItem
import com.example.afinal.data.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    val allFoodItems: Flow<List<FoodItem>>

    init {
        val foodItemDao = FoodDatabase.getDatabase(application).foodItemDao()
        repository = FoodRepository(foodItemDao)
        allFoodItems = repository.allFoodItems
    }

    fun searchFoodItems(query: String): Flow<List<FoodItem>> {
        return repository.searchFoodItems(query)
    }

    suspend fun getFoodItemByBarcode(barcode: String): FoodItem? {
        return repository.getFoodItemByBarcode(barcode)
    }

    fun addFoodItem(name: String, barcode: String?, expiryDate: Date, calories: Int, imageUri: String? = null) {
        viewModelScope.launch {
            val foodItem = FoodItem(
                name = name,
                barcode = barcode,
                expiryDate = expiryDate,
                calories = calories,
                imageUri = imageUri
            )
            repository.insertFoodItem(foodItem)
        }
    }

    fun updateFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            repository.updateFoodItem(foodItem)
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            repository.deleteFoodItem(foodItem)
        }
    }
}