package com.example.tonisfarm.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.data.repository.VacinacaoRepository
import com.example.tonisfarm.data.repository.VermifugacaoRepository
import com.example.tonisfarm.data.repository.PesagemRepository
import com.example.tonisfarm.data.repository.PartoRepository
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.domain.model.TipoEvento
import com.example.tonisfarm.domain.model.Vacinacao
import com.example.tonisfarm.domain.model.Vermifugacao
import com.example.tonisfarm.domain.model.Pesagem
import com.example.tonisfarm.domain.model.Parto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date

@HiltWorker
class EventHistoryUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventoRepository: EventoRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val vermifugacaoRepository: VermifugacaoRepository,
    private val pesagemRepository: PesagemRepository,
    private val partoRepository: PartoRepository,
    private val cowRepository: CowRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val currentDate = Date(currentTime)
            // Usar getAllEventosIncludingCompleted para processar todos os eventos (incluindo concluídos)
            // mas vamos filtrar os concluídos na lógica
            val eventos = eventoRepository.getAllEventosIncludingCompleted().first()

            eventos.forEach { evento ->
                // Pular eventos já concluídos (se ainda existirem no banco)
                if (evento.isCompleted) {
                    return@forEach
                }

                // Verificar se o evento está elegível para conclusão
                if (isEventEligibleForCompletion(evento, currentDate, currentTime)) {
                    processEvent(evento)
                    // Deletar o evento completamente após processá-lo
                    eventoRepository.deleteEvento(evento)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun isEventEligibleForCompletion(evento: Evento, currentDate: Date, currentTime: Long): Boolean {
        val eventDate = evento.dataEvento
        
        // Comparar apenas a data (sem hora)
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
            // Evento COM horário: data atual == data do evento E hora atual >= hora do evento
            if (!isSameDate) {
                return false
            }
            // Construir timestamp completo do evento (data + hora)
            val eventFullCalendar = Calendar.getInstance()
            eventFullCalendar.time = eventDate
            // Extrair hora e minuto do horaEvento
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
            // Evento SEM horário: data atual > data do evento
            isAfterDate
        }
    }


    private suspend fun processEvent(evento: Evento) {
        val cowIds = eventoRepository.getCowIdsByEventoId(evento.id).first()
        
        when (evento.tipoEvento) {
            TipoEvento.VACINACAO -> {
                if (evento.vaccineType != null) {
                    cowIds.forEach { cowId ->
                        createVaccinationHistoryIfNotExists(cowId, evento)
                    }
                }
            }
            TipoEvento.VERMIFUGACAO -> {
                if (evento.dewormingType != null) {
                    cowIds.forEach { cowId ->
                        createDewormingHistoryIfNotExists(cowId, evento)
                    }
                }
            }
            TipoEvento.PESAGEM -> {
                // Sempre criar registro de pesagem, mesmo sem peso (será "Não informado")
                cowIds.forEach { cowId ->
                    createPesagemHistoryIfNotExists(cowId, evento)
                    // Não atualizar peso da vaca ainda, pois o peso não foi informado
                }
            }
            TipoEvento.PARTO -> {
                // Sempre criar registro de parto, mesmo sem quantidade de bezerros (será "Não informado")
                cowIds.forEach { cowId ->
                    createPartoHistoryIfNotExists(cowId, evento)
                }
            }
            TipoEvento.OUTRO -> {
                // Eventos do tipo "Outro" não criam histórico específico
            }
        }
    }

    private suspend fun createVaccinationHistoryIfNotExists(cowId: Long, evento: Evento) {
        // Verificar se já existe um registro com a mesma data e tipo de vacina
        val existingVaccinations = vacinacaoRepository.getVacinacoesByCowId(cowId).first()
        val eventDate = evento.dataEvento
        val vaccineName = evento.vaccineType ?: "Vacina não especificada"
        
        val alreadyExists = existingVaccinations.any { vacinacao ->
            // Comparar apenas a data (sem hora) e o tipo de vacina
            val calendar1 = Calendar.getInstance()
            calendar1.time = vacinacao.date
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
            vacinacaoRepository.insertVacinacao(vacinacao)
        }
    }

    private suspend fun createDewormingHistoryIfNotExists(cowId: Long, evento: Evento) {
        // Verificar se já existe um registro com a mesma data e tipo de vermífugo
        val existingDewormings = vermifugacaoRepository.getVermifugacoesByCowId(cowId).first()
        val eventDate = evento.dataEvento
        val drugName = evento.dewormingType ?: "Vermífugo não especificado"
        
        val alreadyExists = existingDewormings.any { vermifugacao ->
            // Comparar apenas a data (sem hora) e o tipo de vermífugo
            val calendar1 = Calendar.getInstance()
            calendar1.time = vermifugacao.date
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
            vermifugacaoRepository.insertVermifugacao(vermifugacao)
        }
    }

    private suspend fun createPesagemHistoryIfNotExists(cowId: Long, evento: Evento) {
        val existingPesagens = pesagemRepository.getPesagensByCowId(cowId).first()
        val eventDate = evento.dataEvento
        
        // Verificar se já existe um registro com a mesma data
        val alreadyExists = existingPesagens.any { pesagem ->
            val calendar1 = Calendar.getInstance()
            calendar1.time = pesagem.date
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        }

        if (!alreadyExists) {
            // Criar registro com peso "Não informado" (-1.0)
            val pesagem = Pesagem(
                cowId = cowId,
                weightKg = Pesagem.WEIGHT_NOT_INFORMED,
                date = eventDate
            )
            pesagemRepository.insertPesagem(pesagem)
        }
    }

    private suspend fun createPartoHistoryIfNotExists(cowId: Long, evento: Evento) {
        val existingPartos = partoRepository.getPartosByCowId(cowId).first()
        val eventDate = evento.dataEvento
        
        // Verificar se já existe um registro com a mesma data
        val alreadyExists = existingPartos.any { parto ->
            val calendar1 = Calendar.getInstance()
            calendar1.time = parto.date
            val calendar2 = Calendar.getInstance()
            calendar2.time = eventDate
            
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
        }

        if (!alreadyExists) {
            // Criar registro com quantidade "Não informado" (-1)
            val parto = Parto(
                cowId = cowId,
                calvesCount = Parto.CALVES_COUNT_NOT_INFORMED,
                date = eventDate
            )
            partoRepository.insertParto(parto)
        }
    }

    private suspend fun updateCowWeight(cowId: Long) {
        try {
            val pesagens = pesagemRepository.getPesagensByCowId(cowId).first()
            // Filtrar apenas pesagens com peso informado e pegar a mais recente
            val latestPesagem = pesagens
                .filter { it.isWeightInformed }
                .maxWithOrNull(
                    compareBy<Pesagem> { it.date }
                        .thenBy { it.id }
                )
            
            if (latestPesagem != null) {
                val cow = cowRepository.getCowById(cowId)
                cow?.let {
                    val updatedCow = it.copy(pesoKg = latestPesagem.weightKg)
                    cowRepository.updateCow(updatedCow)
                }
            }
        } catch (e: Exception) {
            // Falha ao atualizar peso, mas não quebra o fluxo
        }
    }
}

