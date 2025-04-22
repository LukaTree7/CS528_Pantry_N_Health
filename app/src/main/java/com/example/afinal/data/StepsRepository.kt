package com.example.afinal

import com.example.afinal.data.Steps
import com.example.afinal.data.StepsDao
import kotlinx.coroutines.flow.Flow

class StepsRepository(private val stepsDao: StepsDao) {

    suspend fun getSteps(username: String, date: String): Steps? {
        return stepsDao.getStepsByDate(username, date)
    }

    suspend fun saveSteps(username: String, date: String, steps: Int): Boolean {
        val existingSteps = stepsDao.getStepsByDate(username, date)
        return if (existingSteps != null) {
            val updatedSteps = existingSteps.copy(steps = steps)
            stepsDao.update(updatedSteps)
            true
        } else {
            val newSteps = Steps(username = username, date = date, steps = steps)
            stepsDao.insert(newSteps) > 0
        }
    }

    suspend fun deleteSteps(username: String, date: String): Boolean {
        stepsDao.deleteStepsByDate(username, date)
        return true
    }

    fun getAllSteps(username: String): Flow<List<Steps>> {
        return stepsDao.getAllStepsByUsername(username)
    }
}
