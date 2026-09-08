package com.example.tonisfarm.data.repository

import android.content.Context
import com.example.tonisfarm.data.local.dao.EventCowCrossRefDao
import com.example.tonisfarm.data.local.dao.EventoDao
import com.example.tonisfarm.data.local.entity.EventCowCrossRef
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class EventoRepositoryImpl @Inject constructor(
    private val eventoDao: EventoDao,
    private val eventCowCrossRefDao: EventCowCrossRefDao,
    private val context: Context
) : EventoRepository {
    override fun getEventosByCowId(cowId: Long): Flow<List<Evento>> {
        return eventoDao.getEventosByCowId(cowId).map { entities ->
            entities.map { Evento.fromEntity(it) }
        }
    }

    override fun getAllEventos(): Flow<List<Evento>> {
        return eventoDao.getAllEventos().map { entities ->
            entities.map { Evento.fromEntity(it) }
        }
    }

    override fun getAllEventosIncludingCompleted(): Flow<List<Evento>> {
        return eventoDao.getAllEventosIncludingCompleted().map { entities ->
            entities.map { Evento.fromEntity(it) }
        }
    }

    override fun getAllEventosOrderedByDate(): Flow<List<Evento>> {
        return eventoDao.getAllEventosOrderedByDate().map { entities ->
            entities.map { Evento.fromEntity(it) }
        }
    }

    override fun getUpcomingEventos(startDate: Date, endDate: Date): Flow<List<Evento>> {
        return eventoDao.getUpcomingEventos(startDate.time, endDate.time).map { entities ->
            entities.map { Evento.fromEntity(it) }
        }
    }

    override suspend fun getEventoById(id: Long): Evento? {
        return eventoDao.getEventoById(id)?.let { Evento.fromEntity(it) }
    }

    override suspend fun getLastEventoByCowIdAndType(cowId: Long, tipoEvento: com.example.tonisfarm.domain.model.TipoEvento, currentTime: Long): Evento? {
        return eventoDao.getLastEventoByCowIdAndType(cowId, tipoEvento.name, currentTime)?.let { Evento.fromEntity(it) }
    }

    override suspend fun insertEvento(evento: Evento, cowIds: List<Long>): Long {
        val id = eventoDao.insertEvento(evento.toEntity())
        // Associar vacas ao evento via tabela de junção
        val crossRefs = cowIds.map { cowId -> EventCowCrossRef(eventoId = id, cowId = cowId) }
        eventCowCrossRefDao.insertCrossRefs(crossRefs)
        val eventoWithId = evento.copy(id = id)
        if (eventoWithId.dataAlerta != null) {
            NotificationScheduler.scheduleEventNotification(context, eventoWithId)
        }
        // Agendar conclusão automática do evento
        NotificationScheduler.scheduleEventCompletion(context, eventoWithId)
        return id
    }
    
    suspend fun insertEvento(evento: Evento): Long {
        return insertEvento(evento, emptyList())
    }

    override suspend fun getCowIdsByEventoId(eventoId: Long): Flow<List<Long>> {
        return eventCowCrossRefDao.getCowIdsByEventoId(eventoId)
    }

    override suspend fun removeCowFromEvent(eventoId: Long, cowId: Long) {
        eventCowCrossRefDao.deleteCrossRefByEventoAndCow(eventoId, cowId)
    }

    override suspend fun updateEvento(evento: Evento, cowIds: List<Long>) {
        NotificationScheduler.cancelEventNotification(context, evento.id)
        NotificationScheduler.cancelEventCompletion(context, evento.id)
        eventoDao.updateEvento(evento.toEntity())
        // Atualizar associações
        eventCowCrossRefDao.deleteAllCrossRefsByEventoId(evento.id)
        val crossRefs = cowIds.map { cowId -> EventCowCrossRef(eventoId = evento.id, cowId = cowId) }
        eventCowCrossRefDao.insertCrossRefs(crossRefs)
        if (evento.dataAlerta != null) {
            NotificationScheduler.scheduleEventNotification(context, evento)
        }
        // Reagendar conclusão automática se o evento ainda não foi concluído
        if (!evento.isCompleted) {
            NotificationScheduler.scheduleEventCompletion(context, evento)
        }
    }
    
    suspend fun updateEvento(evento: Evento) {
        updateEvento(evento, emptyList())
    }

    override suspend fun deleteEvento(evento: Evento) {
        NotificationScheduler.cancelEventNotification(context, evento.id)
        NotificationScheduler.cancelEventCompletion(context, evento.id)
        eventCowCrossRefDao.deleteAllCrossRefsByEventoId(evento.id)
        eventoDao.deleteEvento(evento.toEntity())
    }
}

