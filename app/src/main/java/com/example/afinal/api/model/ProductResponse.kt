package com.example.afinal.api.model

data class ProductResponse(
    val product: Product?
)

data class Product(
    val product_name: String?,
    val brands: String?,
    val nutriments: Nutriments?
)

data class Nutriments(
    val energy_kcal: Float?
)
