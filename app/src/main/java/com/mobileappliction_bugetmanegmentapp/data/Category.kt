package com.mobileappliction_bugetmanegmentapp.data

data class Category(
    val id: Int = 0,
    val name: String,
    val color: String, // Hex color code (e.g., "#4CAF50")
    val type: String, // "Income", "Expense", or "Both"
    val isDefault: Boolean = false // true for predefined categories
)
