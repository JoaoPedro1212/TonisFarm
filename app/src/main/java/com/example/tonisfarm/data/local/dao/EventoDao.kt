package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.EventoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {
    @Query("""
        SELECT DISTINCT e.* FROM eventos e
        INNER JOIN event_cow_cross_ref ecr ON e.id = ecr.eventoId
        WHERE ecr.cowId = :cowId AND e.isCompleted = 0
        ORDER BY e.dataEvento ASC
    """)
    fun getEventosByCowId(cowId: Long): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE dataAlerta IS NOT NULL AND isCompleted = 0 ORDER BY dataAlerta ASC")
    fun getAllEventos(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos ORDER BY dataEvento ASC")
    fun getAllEventosIncludingCompleted(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE dataEvento >= :startTime AND dataEvento <= :endTime AND isCompleted = 0 ORDER BY dataEvento ASC")
    fun getUpcomingEventos(startTime: Long, endTime: Long): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE isCompleted = 0 ORDER BY dataEvento ASC")
    fun getAllEventosOrderedByDate(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun getEventoById(id: Long): EventoEntity?

    @Query("""
        SELECT e.* FROM eventos e
        INNER JOIN event_cow_cross_ref ecr ON e.id = ecr.eventoId
        WHERE ecr.cowId = :cowId AND e.tipoEvento = :tipoEvento AND e.dataEvento <= :currentTime
        ORDER BY e.dataEvento DESC LIMIT 1
    """)
    suspend fun getLastEventoByCowIdAndType(cowId: Long, tipoEvento: String, currentTime: Long = System.currentTimeMillis()): EventoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvento(evento: EventoEntity): Long

    @Update
    suspend fun updateEvento(evento: EventoEntity)

    @Delete
    suspend fun deleteEvento(evento: EventoEntity)
}

