package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.PesagemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PesagemDao {
    @Query("SELECT * FROM pesagens WHERE cowId = :cowId ORDER BY date DESC")
    fun getPesagensByCowId(cowId: Long): Flow<List<PesagemEntity>>

    @Query("SELECT * FROM pesagens WHERE cowId = :cowId AND date <= :currentTime ORDER BY date DESC LIMIT 1")
    suspend fun getLastPesagemByCowId(cowId: Long, currentTime: Long = System.currentTimeMillis()): PesagemEntity?

    @Query("SELECT * FROM pesagens WHERE id = :id")
    suspend fun getPesagemById(id: Long): PesagemEntity?

    @Query("""
        SELECT * FROM pesagens 
        WHERE cowId = :cowId 
        AND (:dateFilterType IS NULL OR 
            (:dateFilterType = 'ANTES_DE' AND date < :dateValue) OR
            (:dateFilterType = 'DEPOIS_DE' AND date > :dateValue) OR
            (:dateFilterType = 'NO_DIA' AND date >= :dateStart AND date < :dateEnd))
        AND (:weightFilterType IS NULL OR
            (:weightFilterType = 'MAIS_DE' AND weightKg > :weightValue) OR
            (:weightFilterType = 'MENOS_DE' AND weightKg < :weightValue) OR
            (:weightFilterType = 'IGUAL_A' AND weightKg = :weightValue))
        ORDER BY date DESC
    """)
    fun getFilteredPesagens(
        cowId: Long,
        dateFilterType: String? = null,
        dateValue: Long? = null,
        dateStart: Long? = null,
        dateEnd: Long? = null,
        weightFilterType: String? = null,
        weightValue: Double? = null
    ): Flow<List<PesagemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPesagem(pesagem: PesagemEntity): Long

    @Update
    suspend fun updatePesagem(pesagem: PesagemEntity)

    @Delete
    suspend fun deletePesagem(pesagem: PesagemEntity)
}

