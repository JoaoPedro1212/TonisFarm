package com.example.tonisfarm.ui.events

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.domain.model.TipoEvento
import com.example.tonisfarm.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AllEventsUiState(
    val eventos: List<Evento> = emptyList(),
    val filteredEventos: List<Evento> = emptyList(),
    val selectedTipo: String = "Todos",
    val dateFilterType: String? = null, // "ANTES_DE", "DEPOIS_DE", "NO_DIA"
    val dateFilterValue: java.util.Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AllEventsViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllEventsUiState())
    val uiState: StateFlow<AllEventsUiState> = _uiState.asStateFlow()

    fun loadAllEventos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Disparar atualização de histórico imediatamente ao carregar
            NotificationScheduler.triggerHistoryUpdateNow(context)
            
            try {
                eventoRepository.getAllEventosOrderedByDate().collect { eventos ->
                    _uiState.update { state ->
                        val filtered = applyFilters(eventos, state)
                        state.copy(
                            eventos = eventos,
                            filteredEventos = filtered,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar eventos: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateTipoFilter(tipo: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.eventos, state.copy(selectedTipo = tipo))
            state.copy(selectedTipo = tipo, filteredEventos = filtered)
        }
    }

    fun updateDateFilterType(type: String?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.eventos, state.copy(dateFilterType = type))
            state.copy(dateFilterType = type, filteredEventos = filtered)
        }
    }

    fun updateDateFilterValue(date: Date?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.eventos, state.copy(dateFilterValue = date))
            state.copy(dateFilterValue = date, filteredEventos = filtered)
        }
    }

    private fun applyFilters(eventos: List<Evento>, state: AllEventsUiState): List<Evento> {
        var filtered = eventos

        // Filtro por tipo
        if (state.selectedTipo != "Todos") {
            val tipoEnum = try {
                TipoEvento.valueOf(state.selectedTipo.uppercase())
            } catch (e: Exception) {
                null
            }
            if (tipoEnum != null) {
                filtered = filtered.filter { it.tipoEvento == tipoEnum }
            }
        }

        // Filtro por data
        if (state.dateFilterType != null && state.dateFilterValue != null) {
            val filterDate = state.dateFilterValue.time
            filtered = when (state.dateFilterType) {
                "ANTES_DE" -> filtered.filter { it.dataEvento.time < filterDate }
                "DEPOIS_DE" -> filtered.filter { it.dataEvento.time > filterDate }
                "NO_DIA" -> {
                    val calendar = Calendar.getInstance().apply { time = state.dateFilterValue }
                    val startOfDay = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val endOfDay = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    filtered.filter { 
                        val eventTime = it.dataEvento.time
                        eventTime >= startOfDay && eventTime <= endOfDay
                    }
                }
                else -> filtered
            }
        }

        return filtered
    }

    fun deleteEvento(evento: Evento) {
        viewModelScope.launch {
            try {
                eventoRepository.deleteEvento(evento)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao excluir evento: ${e.message}")
                }
            }
        }
    }
}

