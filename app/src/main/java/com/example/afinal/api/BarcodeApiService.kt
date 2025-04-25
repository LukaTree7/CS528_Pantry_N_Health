package com.example.afinal.api

import com.example.afinal.api.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface BarcodeApiService {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse
}
