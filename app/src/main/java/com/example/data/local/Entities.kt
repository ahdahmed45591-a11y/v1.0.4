package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val whatsapp: String = "", // WhatsApp contact for KYC verification
    val kycStep: Int = 0, // 0 = Welcome/Login, 1 = Personal Details Form, 2 = Document verification (KYC), 3 = Proof of address, 4 = Signature contract, 5 = Signed & Verified
    val cashBalance: Double = 0.0, // Initial balance in FCFA is 0
    val portfolioValue: Double = 0.0, // Initial portfolio value is 0
    val isPremium: Boolean = false,
    val membershipDate: String = ""
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "DEPOSIT", "BUY", "SELL"
    val title: String,
    val date: String,
    val reference: String,
    val status: String, // "TERMINÉ", "EN ATTENTE", "ANNULÉ"
    val amount: Double,
    val sharesQty: Int = 0,
    val stockTicker: String = ""
)

@Entity(tableName = "holdings")
data class HoldingsEntity(
    @PrimaryKey val ticker: String,
    val companyName: String,
    val sharesCount: Int,
    val averagePrice: Double,
    val currentPrice: Double,
    val changePercent: Double,
    val sector: String
)
