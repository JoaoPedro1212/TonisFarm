package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.Evento
import kotlinx.coroutines.flow.Flow

interface EventoRepository {
    fun getEventosByCowId(cowId: Long): Flow<List<Evento>>
    fun getAllEventos(): Flow<List<Evento>> // Retorna apenas eventos não concluídos
    fun getAllEventosIncludingCompleted(): Flow<List<Evento>> // Retorna todos os eventos (incluindo concluídos) - usado pelo Worker
    fun getAllEventosOrderedByDate(): Flow<List<Evento>>
    fun getUpcomingEventos(startDate: java.util.Date, endDate: java.util.Date): Flow<List<Evento>>
    suspend fun getEventoById(id: Long): Evento?
    suspend fun getLastEventoByCowIdAndType(cowId: Long, tipoEvento: com.example.tonisfarm.domain.model.TipoEvento, currentTime: Long = System.currentTimeMillis()): Evento?
    suspend fun insertEvento(evento: Evento, cowIds: List<Long>): Long
    suspend fun getCowIdsByEventoId(eventoId: Long): Flow<List<Long>>
    suspend fun removeCowFromEvent(eventoId: Long, cowId: Long)
    suspend fun updateEvento(evento: Evento, cowIds: List<Long>)
    suspend fun deleteEvento(evento: Evento)
}

