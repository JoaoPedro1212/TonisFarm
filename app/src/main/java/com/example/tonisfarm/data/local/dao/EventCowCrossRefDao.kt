package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.EventCowCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface EventCowCrossRefDao {
    @Query("SELECT cowId FROM event_cow_cross_ref WHERE eventoId = :eventoId")
    fun getCowIdsByEventoId(eventoId: Long): Flow<List<Long>>

    @Query("SELECT eventoId FROM event_cow_cross_ref WHERE cowId = :cowId")
    fun getEventoIdsByCowId(cowId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: EventCowCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<EventCowCrossRef>)

    @Delete
    suspend fun deleteCrossRef(crossRef: EventCowCrossRef)

    @Query("DELETE FROM event_cow_cross_ref WHERE eventoId = :eventoId AND cowId = :cowId")
    suspend fun deleteCrossRefByEventoAndCow(eventoId: Long, cowId: Long)

    @Query("DELETE FROM event_cow_cross_ref WHERE eventoId = :eventoId")
    suspend fun deleteAllCrossRefsByEventoId(eventoId: Long)
}

