package com.example.tonisfarm.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.domain.model.Cow
import com.example.tonisfarm.domain.model.Evento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(
    val evento: Evento? = null,
    val cows: List<Cow> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val cowRepository: CowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun loadEvento(eventoId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val evento = eventoRepository.getEventoById(eventoId)
                _uiState.update { it.copy(evento = evento, isLoading = false) }

                if (evento != null) {
                    // Carregar vacas associadas ao evento
                    viewModelScope.launch {
                        eventoRepository.getCowIdsByEventoId(eventoId).collect { cowIds ->
                            val cows = mutableListOf<Cow>()
                            for (cowId in cowIds) {
                                cowRepository.getCowById(cowId)?.let { cows.add(it) }
                            }
                            _uiState.update { it.copy(cows = cows) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar: ${e.message}"
                    )
                }
            }
        }
    }

    fun removeCowFromEvent(eventoId: Long, cowId: Long) {
        viewModelScope.launch {
            try {
                eventoRepository.removeCowFromEvent(eventoId, cowId)
                // A lista será atualizada automaticamente pelo Flow
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao remover vaca: ${e.message}")
                }
            }
        }
    }
}

