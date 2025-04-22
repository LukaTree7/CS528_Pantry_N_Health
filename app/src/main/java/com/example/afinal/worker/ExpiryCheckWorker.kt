package com.example.afinal.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.afinal.data.FoodDatabase
import com.example.afinal.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("ExpiryCheck", "Starting expiry check worker")


            val database = FoodDatabase.getDatabase(applicationContext)
            val foodItemDao = database.foodItemDao()

            val allItems = foodItemDao.getAllFoodItemsList()
            Log.d("ExpiryCheck", "Found ${allItems.size} total items in database")


            val expiringItems = allItems.filter {
                NotificationHelper.shouldNotifyForItem(it)
            }

            Log.d("ExpiryCheck", "Found ${expiringItems.size} expiring items to notify about")

            if (expiringItems.isNotEmpty()) {

                NotificationHelper.showSmartExpiryNotifications(
                    applicationContext,
                    expiringItems
                )

                NotificationHelper.showExpiryNotification(
                    applicationContext,
                    expiringItems
                )
            } else {
                Log.d("ExpiryCheck", "No items need notification at this time")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("ExpiryCheck", "Error in expiry check worker", e)
            Result.failure()
        }
    }
}