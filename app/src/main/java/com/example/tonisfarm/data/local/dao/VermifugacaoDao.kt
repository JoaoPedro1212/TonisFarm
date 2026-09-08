package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.VermifugacaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VermifugacaoDao {
    @Query("SELECT * FROM vermifugacoes WHERE cowId = :cowId ORDER BY date DESC")
    fun getVermifugacoesByCowId(cowId: Long): Flow<List<VermifugacaoEntity>>

    @Query("SELECT * FROM vermifugacoes WHERE cowId = :cowId AND date <= :currentTime ORDER BY date DESC LIMIT 1")
    suspend fun getLastVermifugacaoByCowId(cowId: Long, currentTime: Long = System.currentTimeMillis()): VermifugacaoEntity?

    @Query("SELECT * FROM vermifugacoes WHERE id = :id")
    suspend fun getVermifugacaoById(id: Long): VermifugacaoEntity?

    @Query("""
        SELECT * FROM vermifugacoes 
        WHERE cowId = :cowId 
        AND (:drugName IS NULL OR drugName = :drugName)
        AND (:dateFilterType IS NULL OR 
            (:dateFilterType = 'ANTES_DE' AND date < :dateValue) OR
            (:dateFilterType = 'DEPOIS_DE' AND date > :dateValue) OR
            (:dateFilterType = 'NO_DIA' AND date >= :dateStart AND date < :dateEnd))
        ORDER BY date DESC
    """)
    fun getFilteredVermifugacoes(
        cowId: Long,
        drugName: String? = null,
        dateFilterType: String? = null,
        dateValue: Long? = null,
        dateStart: Long? = null,
        dateEnd: Long? = null
    ): Flow<List<VermifugacaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVermifugacao(vermifugacao: VermifugacaoEntity): Long

    @Update
    suspend fun updateVermifugacao(vermifugacao: VermifugacaoEntity)

    @Delete
    suspend fun deleteVermifugacao(vermifugacao: VermifugacaoEntity)
}

