package com.example.afinal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodItem::class, Login::class, Steps::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun loginDao(): LoginDao
    abstract fun stepsDao(): StepsDao

    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "food_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}