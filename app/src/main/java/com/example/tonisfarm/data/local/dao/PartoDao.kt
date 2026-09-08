package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.PartoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartoDao {
    @Query("SELECT * FROM partos WHERE cowId = :cowId ORDER BY date DESC")
    fun getPartosByCowId(cowId: Long): Flow<List<PartoEntity>>

    @Query("SELECT * FROM partos WHERE cowId = :cowId AND date <= :currentTime ORDER BY date DESC LIMIT 1")
    suspend fun getLastPartoByCowId(cowId: Long, currentTime: Long = System.currentTimeMillis()): PartoEntity?

    @Query("SELECT * FROM partos WHERE id = :id")
    suspend fun getPartoById(id: Long): PartoEntity?

    @Query("""
        SELECT * FROM partos 
        WHERE cowId = :cowId 
        AND (:dateFilterType IS NULL OR 
            (:dateFilterType = 'ANTES_DE' AND date < :dateValue) OR
            (:dateFilterType = 'DEPOIS_DE' AND date > :dateValue) OR
            (:dateFilterType = 'NO_DIA' AND date >= :dateStart AND date < :dateEnd))
        AND (:calvesCountFilter IS NULL OR calvesCount = :calvesCountFilter)
        ORDER BY date DESC
    """)
    fun getFilteredPartos(
        cowId: Long,
        dateFilterType: String? = null,
        dateValue: Long? = null,
        dateStart: Long? = null,
        dateEnd: Long? = null,
        calvesCountFilter: Int? = null
    ): Flow<List<PartoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParto(parto: PartoEntity): Long

    @Update
    suspend fun updateParto(parto: PartoEntity)

    @Delete
    suspend fun deleteParto(parto: PartoEntity)
}

