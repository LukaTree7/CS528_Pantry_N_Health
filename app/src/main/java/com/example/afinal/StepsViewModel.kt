package com.example.afinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.FoodDatabase
import com.example.afinal.data.Steps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StepsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StepsRepository

    init {
        val stepsDao = FoodDatabase.getDatabase(application).stepsDao()
        repository = StepsRepository(stepsDao)
    }

    suspend fun getSteps(username: String, date: String): Steps? {
        return repository.getSteps(username, date)
    }

    fun saveSteps(username: String, date: String, steps: Int) {
        viewModelScope.launch {
            repository.saveSteps(username, date, steps)
        }
    }

    fun deleteSteps(username: String, date: String) {
        viewModelScope.launch {
            repository.deleteSteps(username, date)
        }
    }

    fun getAllSteps(username: String): Flow<List<Steps>> {
        return repository.getAllSteps(username)
    }
}
