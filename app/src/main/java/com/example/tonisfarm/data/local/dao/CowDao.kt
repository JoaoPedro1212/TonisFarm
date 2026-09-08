package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.CowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CowDao {
    @Query("SELECT * FROM cows ORDER BY id DESC")
    fun getAllCows(): Flow<List<CowEntity>>

    @Query("SELECT * FROM cows WHERE id = :id")
    suspend fun getCowById(id: Long): CowEntity?

    @Query("SELECT * FROM cows WHERE identificacao LIKE '%' || :search || '%' OR nome LIKE '%' || :search || '%'")
    fun searchCows(search: String): Flow<List<CowEntity>>

    @Query("SELECT * FROM cows WHERE estaPrenha = 1 ORDER BY id DESC")
    fun getPregnantCows(): Flow<List<CowEntity>>

    @Query("SELECT COUNT(*) FROM cows")
    fun getTotalCowsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cows WHERE estaPrenha = 1")
    fun getPregnantCowsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCow(cow: CowEntity): Long

    @Update
    suspend fun updateCow(cow: CowEntity)

    @Delete
    suspend fun deleteCow(cow: CowEntity)
}

