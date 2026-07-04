package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "default_user",
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "default_user",
    val ticker: String, // e.g., "BBCA", "TLKM"
    val lotCount: Int,
    val averageBuyPrice: Double,
    val lastFetchedPrice: Double = averageBuyPrice
)
