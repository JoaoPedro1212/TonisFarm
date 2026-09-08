package com.example.tonisfarm.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.data.repository.FinancialTransactionRepository
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val totalCows: Int = 0,
    val pregnantCows: Int = 0,
    val upcomingEvents: List<Evento> = emptyList(),
    val currentMonthFinancialSituation: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val cowRepository: CowRepository,
    private val eventoRepository: EventoRepository,
    private val financialTransactionRepository: FinancialTransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Disparar atualização de histórico imediatamente ao carregar
            NotificationScheduler.triggerHistoryUpdateNow(context)
            
            // Processar recorrências ANTES de começar a coletar o Flow
            // Isso garante que os dados estejam atualizados quando o Flow começar a emitir
            financialTransactionRepository.processRecurrences()

            combine(
                cowRepository.getTotalCowsCount(),
                cowRepository.getPregnantCowsCount(),
                eventoRepository.getAllEventosOrderedByDate(),
                financialTransactionRepository.getCurrentMonthSummary()
            ) { totalCows, pregnantCows, allEvents, monthSummary ->
                // Recalcular agora e currentTime a cada emissão do Flow para garantir valores atualizados
                val now = java.util.Date()
                val currentTime = System.currentTimeMillis()
                
                // Filtrar apenas eventos que ainda não passaram
                // Eventos concluídos são deletados, então não precisamos verificar isCompleted
                val eligibleEvents = allEvents.filter { evento ->
                    isEventStillUpcoming(evento, now, currentTime)
                }
                
                // Ordenar por data e horário
                val sortedEvents = eligibleEvents.sortedWith(
                    compareBy<Evento> { it.dataEvento }
                        .thenBy { evento ->
                            // Eventos com horário vêm antes dos sem horário no mesmo dia
                            if (evento.horaEvento != null) {
                                evento.horaEvento.time
                            } else {
                                Long.MAX_VALUE // Sem horário vai para o final do dia
                            }
                        }
                )
                
                // Pegar todos os eventos da data mais próxima
                val upcomingEvents = getEventsFromClosestDate(sortedEvents)

                DashboardUiState(
                    totalCows = totalCows,
                    pregnantCows = pregnantCows,
                    upcomingEvents = upcomingEvents,
                    currentMonthFinancialSituation = monthSummary.lucro,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private fun isEventStillUpcoming(evento: Evento, currentDate: Date, currentTime: Long): Boolean {
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
        val isBeforeDate = (currentYear < eventYear) || (currentYear == eventYear && currentDayOfYear < eventDayOfYear)
        
        return if (evento.horaEvento != null) {
            // Evento COM horário: incluir se ainda não passou o horário
            if (isBeforeDate) {
                true // Evento futuro
            } else if (isSameDate) {
                // Construir timestamp completo do evento (data + hora)
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
                currentTime < eventFullTime // Ainda não passou o horário
            } else {
                false // Já passou
            }
        } else {
            // Evento SEM horário: incluir se é hoje ou futuro
            isSameDate || isBeforeDate
        }
    }

    private fun getEventsFromClosestDate(events: List<Evento>): List<Evento> {
        if (events.isEmpty()) return emptyList()
        
        // Verificar se os próximos 5 eventos estão todos no mesmo dia
        val primeiros5Eventos = events.take(5)
        if (primeiros5Eventos.size == 5) {
            val primeiroEvento = primeiros5Eventos.first()
            val primeiroEventoCalendar = Calendar.getInstance()
            primeiroEventoCalendar.time = primeiroEvento.dataEvento
            val primeiroAno = primeiroEventoCalendar.get(Calendar.YEAR)
            val primeiroDiaDoAno = primeiroEventoCalendar.get(Calendar.DAY_OF_YEAR)
            
            val todosNoMesmoDia = primeiros5Eventos.all { evento ->
                val eventCalendar = Calendar.getInstance()
                eventCalendar.time = evento.dataEvento
                eventCalendar.get(Calendar.YEAR) == primeiroAno &&
                eventCalendar.get(Calendar.DAY_OF_YEAR) == primeiroDiaDoAno
            }
            
            // Se os próximos 5 eventos estiverem todos no mesmo dia, mostrar todos os 5
            if (todosNoMesmoDia) {
                return primeiros5Eventos
            }
        }
        
        // Pegar a data do primeiro evento (mais próximo) - primeiraData
        val firstEvent = events.first()
        val firstEventDate = firstEvent.dataEvento
        val firstEventCalendar = Calendar.getInstance()
        firstEventCalendar.time = firstEventDate
        val firstEventYear = firstEventCalendar.get(Calendar.YEAR)
        val firstEventDayOfYear = firstEventCalendar.get(Calendar.DAY_OF_YEAR)
        
        // Coletar todos os eventos do primeiro dia (eventosPrimeiroDia)
        val eventosPrimeiroDia = events.filter { evento ->
            val eventCalendar = Calendar.getInstance()
            eventCalendar.time = evento.dataEvento
            eventCalendar.get(Calendar.YEAR) == firstEventYear &&
            eventCalendar.get(Calendar.DAY_OF_YEAR) == firstEventDayOfYear
        }
        
        // Se eventosPrimeiroDia.size >= 3
        if (eventosPrimeiroDia.size >= 3) {
            // Se houver até 5 eventos nesse mesmo dia, mostrar todos eles
            // Se houver mais de 5, mostrar somente os 5 primeiros (mais próximos pelo horário)
            return eventosPrimeiroDia.take(5)
        }
        
        // Se eventosPrimeiroDia.size < 3
        // Sempre mostrar todos os eventos do primeiro dia
        val resultado = eventosPrimeiroDia.toMutableList()
        
        // Completar a lista com eventos dos dias seguintes, na ordem cronológica, até ter no máximo 3 eventos no total
        val eventosRestantes = events.filter { evento ->
            val eventCalendar = Calendar.getInstance()
            eventCalendar.time = evento.dataEvento
            val eventYear = eventCalendar.get(Calendar.YEAR)
            val eventDayOfYear = eventCalendar.get(Calendar.DAY_OF_YEAR)
            
            // Evento não está no primeiro dia
            !(eventYear == firstEventYear && eventDayOfYear == firstEventDayOfYear)
        }
        
        // Adicionar eventos dos dias seguintes até completar 3 no total
        val limiteTotal = 3
        
        for (evento in eventosRestantes) {
            if (resultado.size >= limiteTotal) break
            
            resultado.add(evento)
        }
        
        return resultado
    }

    fun refresh() {
        loadDashboardData()
    }
}

