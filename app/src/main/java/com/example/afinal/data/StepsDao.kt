package com.example.afinal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsDao {
    @Insert
    suspend fun insert(steps: Steps): Long

    @Query("SELECT * FROM steps WHERE username = :username AND date = :date LIMIT 1")
    suspend fun getStepsByDate(username: String, date: String): Steps?

    @Update
    suspend fun update(steps: Steps)

    @Query("DELETE FROM steps WHERE username = :username AND date = :date")
    suspend fun deleteStepsByDate(username: String, date: String)

    @Query("SELECT * FROM steps WHERE username = :username ORDER BY date ASC")
    fun getAllStepsByUsername(username: String): Flow<List<Steps>>
}
