package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BourseDao {
    // User Profile Queries
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(user: UserEntity)

    // Transaction Queries
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM holdings")
    suspend fun clearAllHoldings()


    // Stock Holdings Queries
    @Query("SELECT * FROM holdings")
    fun getAllHoldingsFlow(): Flow<List<HoldingsEntity>>

    @Query("SELECT * FROM holdings WHERE ticker = :ticker LIMIT 1")
    suspend fun getHoldingByTicker(ticker: String): HoldingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingsEntity)

    @Update
    suspend fun updateHolding(holding: HoldingsEntity)

    @Delete
    suspend fun deleteHolding(holding: HoldingsEntity)
}
