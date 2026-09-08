package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.VacinacaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VacinacaoDao {
    @Query("SELECT * FROM vacinacoes WHERE cowId = :cowId ORDER BY date DESC")
    fun getVacinacoesByCowId(cowId: Long): Flow<List<VacinacaoEntity>>

    @Query("SELECT * FROM vacinacoes WHERE cowId = :cowId AND date <= :currentTime ORDER BY date DESC LIMIT 1")
    suspend fun getLastVacinacaoByCowId(cowId: Long, currentTime: Long = System.currentTimeMillis()): VacinacaoEntity?

    @Query("SELECT * FROM vacinacoes WHERE id = :id")
    suspend fun getVacinacaoById(id: Long): VacinacaoEntity?

    @Query("""
        SELECT * FROM vacinacoes 
        WHERE cowId = :cowId 
        AND (:vaccineName IS NULL OR vaccineName = :vaccineName)
        AND (:dateFilterType IS NULL OR 
            (:dateFilterType = 'ANTES_DE' AND date < :dateValue) OR
            (:dateFilterType = 'DEPOIS_DE' AND date > :dateValue) OR
            (:dateFilterType = 'NO_DIA' AND date >= :dateStart AND date < :dateEnd))
        ORDER BY date DESC
    """)
    fun getFilteredVacinacoes(
        cowId: Long,
        vaccineName: String? = null,
        dateFilterType: String? = null,
        dateValue: Long? = null,
        dateStart: Long? = null,
        dateEnd: Long? = null
    ): Flow<List<VacinacaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacinacao(vacinacao: VacinacaoEntity): Long

    @Update
    suspend fun updateVacinacao(vacinacao: VacinacaoEntity)

    @Delete
    suspend fun deleteVacinacao(vacinacao: VacinacaoEntity)
}

