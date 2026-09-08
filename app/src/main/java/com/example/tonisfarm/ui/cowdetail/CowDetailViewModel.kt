package com.example.tonisfarm.ui.cowdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.data.repository.VacinacaoRepository
import com.example.tonisfarm.data.repository.VermifugacaoRepository
import com.example.tonisfarm.data.repository.PesagemRepository
import com.example.tonisfarm.data.repository.PartoRepository
import com.example.tonisfarm.domain.model.Cow
import com.example.tonisfarm.domain.model.Evento
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class CowDetailUiState(
    val cow: Cow? = null,
    val eventos: List<Evento> = emptyList(),
    val lastVermifugacao: Date? = null,
    val lastPesagem: Date? = null,
    val lastPesagemWeight: Double? = null,
    val lastParto: Date? = null,
    val lastPartoCalvesCount: Int? = null,
    val lastVacinacao: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    // Calcula a data a exibir considerando override manual ou evento passado
    fun getDisplayDate(manualDate: Date?, eventDate: Date?): Date? {
        return manualDate ?: eventDate
    }
}

@HiltViewModel
class CowDetailViewModel @Inject constructor(
    private val cowRepository: CowRepository,
    private val eventoRepository: EventoRepository,
    private val vacinacaoRepository: VacinacaoRepository,
    private val vermifugacaoRepository: VermifugacaoRepository,
    private val pesagemRepository: PesagemRepository,
    private val partoRepository: PartoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CowDetailUiState())
    val uiState: StateFlow<CowDetailUiState> = _uiState.asStateFlow()

    fun loadCow(cowId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cow = cowRepository.getCowById(cowId)
                _uiState.update { it.copy(cow = cow, isLoading = false) }

                if (cow != null) {
                    // Carregar histórico resumido dos novos repositórios
                    viewModelScope.launch {
                        val lastVacinacao = vacinacaoRepository.getLastVacinacaoByCowId(cowId)
                        val lastVermifugacao = vermifugacaoRepository.getLastVermifugacaoByCowId(cowId)
                        val lastPesagem = pesagemRepository.getLastPesagemByCowId(cowId)
                        val lastParto = partoRepository.getLastPartoByCowId(cowId)
                        
                        _uiState.update { 
                            it.copy(
                                lastVacinacao = lastVacinacao?.date ?: cow.ultimaVacinacaoManual,
                                lastVermifugacao = lastVermifugacao?.date ?: cow.ultimaVermifugacaoManual,
                                lastPesagem = lastPesagem?.date ?: cow.ultimaPesagemManual,
                                lastPesagemWeight = lastPesagem?.weightKg,
                                lastParto = lastParto?.date ?: cow.ultimoPartoManual,
                                lastPartoCalvesCount = lastParto?.calvesCount
                            )
                        }
                    }
                    
                    // Carregar eventos em outra coroutine (Flow)
                    viewModelScope.launch {
                        eventoRepository.getEventosByCowId(cowId).collect { eventos ->
                            _uiState.update { it.copy(eventos = eventos) }
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

    fun deleteCow(onSuccess: () -> Unit) {
        val cow = _uiState.value.cow ?: return
        viewModelScope.launch {
            try {
                cowRepository.deleteCow(cow)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao excluir: ${e.message}")
                }
            }
        }
    }

    fun removeCowFromEvent(eventoId: Long, cowId: Long) {
        viewModelScope.launch {
            try {
                eventoRepository.removeCowFromEvent(eventoId, cowId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao remover vaca do evento: ${e.message}")
                }
            }
        }
    }

    fun updateManualHistoryDate(
        cowId: Long,
        tipo: String,
        date: Date?
    ) {
        viewModelScope.launch {
            try {
                val cow = cowRepository.getCowById(cowId) ?: return@launch
                val updatedCow = when (tipo) {
                    "VACINACAO" -> cow.copy(ultimaVacinacaoManual = date)
                    "VERMIFUGACAO" -> cow.copy(ultimaVermifugacaoManual = date)
                    "PESAGEM" -> cow.copy(ultimaPesagemManual = date)
                    "PARTO" -> cow.copy(ultimoPartoManual = date)
                    else -> cow
                }
                cowRepository.updateCow(updatedCow)
                loadCow(cowId) // Recarregar para atualizar UI
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao atualizar histórico: ${e.message}")
                }
            }
        }
    }

}

