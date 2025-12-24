package com.mobileappliction_bugetmanegmentapp.data

data class Transaction(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val date: String,
    val note: String = "",
    val category: String = "Uncategorized", // Category name for display
    val categoryColor: String = "#757575" // Hex color for UI
)
