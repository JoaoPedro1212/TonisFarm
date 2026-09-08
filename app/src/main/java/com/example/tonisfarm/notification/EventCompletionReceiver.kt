package com.example.tonisfarm.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.tonisfarm.data.local.AppDatabase
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.domain.model.TipoEvento
import com.example.tonisfarm.domain.model.Vacinacao
import com.example.tonisfarm.domain.model.Vermifugacao
import com.example.tonisfarm.domain.model.Pesagem
import com.example.tonisfarm.domain.model.Parto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class EventCompletionReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_COMPLETE_EVENT) {
            val eventoId = intent.getLongExtra(EXTRA_EVENTO_ID, -1)
            if (eventoId != -1L) {
                scope.launch {
                    try {
                        val database = AppDatabase.getDatabase(context)
                        val eventoEntity = database.eventoDao().getEventoById(eventoId)
                        eventoEntity?.let { entity ->
                            val evento = Evento.fromEntity(entity)
                            
                            // Verificar se o evento está elegível para conclusão
                            val currentTime = System.currentTimeMillis()
                            val currentDate = java.util.Date(currentTime)
                            
                            if (isEventEligibleForCompletion(evento, currentDate, currentTime)) {
                                // Processar o evento
                                processEvent(context, database, evento)
                                
                                // Deletar o evento completamente após processá-lo
                                // Cancelar notificações e alarmes primeiro
                                NotificationScheduler.cancelEventNotification(context, evento.id)
                                NotificationScheduler.cancelEventCompletion(context, evento.id)
                                
                                // Deletar associações com vacas
                                database.eventCowCrossRefDao().deleteAllCrossRefsByEventoId(evento.id)
                                
                                // Deletar o evento
                                database.eventoDao().deleteEvento(entity)
                            }
                        }
                    } catch (e: Exception) {
                        // Falha ao processar evento, mas não quebra
                    }
                }
            }
        }
    }

    private fun isEventEligibleForCompletion(evento: Evento, currentDate: java.util.Date, currentTime: Long): Boolean {
        val eventDate = evento.dataEvento
        
        val eventCalendar = Calendar.getInstance()
        eventCalendar.time = eventDate
        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = currentDate
        
        val eventYear = eventCalendar.get(Calendar.YEAR)
        val eventDayOfYear = eventCalendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentDayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR)
        
        val isSameDate = (eventYear == currentYear && eventDayOfYear == currentDayOfYear)
        val isAfterDate = (currentYear > eventYear) || (currentYear == eventYear && currentDayOfYear > eventDayOfYear)
        
        return if (evento.horaEvento != null) {
            if (!isSameDate) {
                return false
            }
            val eventFullCalendar = Calendar.getInstance()
            eventFullCalendar.time = eventDate
            val horaEventoCalendar = Calendar.getInstance()
            horaEventoCalendar.time = evento.horaEvento
            val eventHour = horaEventoCalendar.get(Calendar.HOUR_OF_DAY)
            val eventMinute = horaEventoCalendar.get(Calendar.MINUTE)
            eventFullCalendar.set(Calendar.HOUR_OF_DAY, eventHour)
            eventFullCalendar.set(Calendar.MINUTE, eventMinute)
            eventFullCalendar.set(Calendar.SECOND, 0)
            eventFullCalendar.set(Calendar.MILLISECOND, 0)
            val eventFullTime = eventFullCalendar.timeInMillis
            currentTime >= eventFullTime
        } else {
            isAfterDate
        }
    }

    private suspend fun processEvent(context: Context, database: AppDatabase, evento: Evento) {
        val cowIds = database.eventCowCrossRefDao().getCowIdsByEventoId(evento.id).first()
        
        when (evento.tipoEvento) {
            TipoEvento.VACINACAO -> {
                if (evento.vaccineType != null) {
                    cowIds.forEach { cowId ->
                        createVaccinationHistoryIfNotExists(database, cowId, evento)
                    }
                }
            }
            TipoEvento.VERMIFUGACAO -> {
                if (evento.dewormingType != null) {
                    cowIds.forEach { cowId ->
                        createDewormingHistoryIfNotExists(database, cowId, evento)
                    }
                }
            }
            TipoEvento.PESAGEM -> {
                cowIds.forEach { cowId ->
                    // Verificar se existe registro manual nas últimas 48 horas
                    if (!hasManualPesagemInLast48Hours(database, cowId, evento)) {
                        createPesagemHistoryIfNotExists(database, cowId, evento)
                    }
                }
            }
            TipoEvento.PARTO -> {
                cowIds.forEach { cowId ->
                    // Verificar se existe registro manual nas últimas 48 horas
                    if (!hasManualPartoInLast48Hours(database, cowId, evento)) {
                        createPartoHistoryIfNotExists(database, cowId, evento)
                    }
                }
            }
            TipoEvento.OUTRO -> {
                // Eventos do tipo "Outro" não criam histórico específico
            }
        }
    }

    private suspend fun hasManualPesagemInLast48Hours(database: AppDatabase, cowId: Long, evento: Evento): Boolean {
        val eventDate = evento.dataEvento
        
        val existingPesagens = database.pesagemDao().getPesagensByCowId(cowId).first()
        
        // Ordenar por ID para pegar os mais recentes (IDs maiores = mais recentes)
        val sortedPesagens = existingPesagens.sortedByDescending { it.id }
        val recentPesagens = sortedPesagens.take(20) // Últimos 20 registros (heurística para "recentes")
        
        // Verificar se existe um registro manual (com peso informado) na mesma data entre os recentes
        return recentPesagens.any { pesagem ->
            val pesagemDate = java.util.Date(pesagem.date)
            val calendar1 = Calendar.getInstance()
            calendar1.time = pesagemDate
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            
            val sameDate = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                          calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
            
            // Verificar se é manual (peso informado, não é "Não informado")
            val isManual = pesagem.weightKg >= 0
            
            sameDate && isManual
        }
    }

    private suspend fun hasManualPartoInLast48Hours(database: AppDatabase, cowId: Long, evento: Evento): Boolean {
        val eventDate = evento.dataEvento
        
        val existingPartos = database.partoDao().getPartosByCowId(cowId).first()
        
        // Ordenar por ID para pegar os mais recentes (IDs maiores = mais recentes)
        val sortedPartos = existingPartos.sortedByDescending { it.id }
        val recentPartos = sortedPartos.take(20) // Últimos 20 registros (heurística para "recentes")
        
        // Verificar se existe um registro manual (com quantidade informada) na mesma data entre os recentes
        return recentPartos.any { parto ->
            val partoDate = java.util.Date(parto.date)
            val calendar1 = Calendar.getInstance()
            calendar1.time = partoDate
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            
            val sameDate = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                          calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
            
            // Verificar se é manual (quantidade informada, não é "Não informado")
            val isManual = parto.calvesCount >= 0
            
            sameDate && isManual
        }
    }

    private suspend fun createVaccinationHistoryIfNotExists(database: AppDatabase, cowId: Long, evento: Evento) {
        val existingVaccinations = database.vacinacaoDao().getVacinacoesByCowId(cowId).first()
        val eventDate = evento.dataEvento
        val vaccineName = evento.vaccineType ?: "Vacina não especificada"
        
        val alreadyExists = existingVaccinations.any { vacinacao ->
            val calendar1 = Calendar.getInstance()
            calendar1.time = java.util.Date(vacinacao.date)
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR) &&
            vacinacao.vaccineName == vaccineName
        }
        
        if (!alreadyExists) {
            val vacinacao = Vacinacao(
                cowId = cowId,
                vaccineName = vaccineName,
                date = eventDate
            )
            database.vacinacaoDao().insertVacinacao(vacinacao.toEntity())
        }
    }

    private suspend fun createDewormingHistoryIfNotExists(database: AppDatabase, cowId: Long, evento: Evento) {
        val existingDewormings = database.vermifugacaoDao().getVermifugacoesByCowId(cowId).first()
        val eventDate = evento.dataEvento
        val drugName = evento.dewormingType ?: "Vermífugo não especificado"
        
        val alreadyExists = existingDewormings.any { vermifugacao ->
            val calendar1 = Calendar.getInstance()
            calendar1.time = java.util.Date(vermifugacao.date)
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR) &&
            vermifugacao.drugName == drugName
        }
        
        if (!alreadyExists) {
            val vermifugacao = Vermifugacao(
                cowId = cowId,
                drugName = drugName,
                date = eventDate
            )
            database.vermifugacaoDao().insertVermifugacao(vermifugacao.toEntity())
        }
    }

    private suspend fun createPesagemHistoryIfNotExists(database: AppDatabase, cowId: Long, evento: Evento) {
        val existingPesagens = database.pesagemDao().getPesagensByCowId(cowId).first()
        val eventDate = evento.dataEvento
        
        val alreadyExists = existingPesagens.any { pesagem ->
            val calendar1 = Calendar.getInstance()
            calendar1.time = java.util.Date(pesagem.date)
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        }
        
        if (!alreadyExists) {
            val pesagem = Pesagem(
                cowId = cowId,
                weightKg = Pesagem.WEIGHT_NOT_INFORMED,
                date = eventDate
            )
            database.pesagemDao().insertPesagem(pesagem.toEntity())
        }
    }

    private suspend fun createPartoHistoryIfNotExists(database: AppDatabase, cowId: Long, evento: Evento) {
        val existingPartos = database.partoDao().getPartosByCowId(cowId).first()
        val eventDate = evento.dataEvento
        
        val alreadyExists = existingPartos.any { parto ->
            val calendar1 = Calendar.getInstance()
            calendar1.time = java.util.Date(parto.date)
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        }
        
        if (!alreadyExists) {
            val parto = Parto(
                cowId = cowId,
                calvesCount = Parto.CALVES_COUNT_NOT_INFORMED,
                date = eventDate
            )
            database.partoDao().insertParto(parto.toEntity())
        }
    }
}

