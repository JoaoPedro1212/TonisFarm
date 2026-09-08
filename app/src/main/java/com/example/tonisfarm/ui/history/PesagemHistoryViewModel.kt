package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.PesagemRepository
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.domain.model.Pesagem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class PesagemHistoryUiState(
    val pesagens: List<Pesagem> = emptyList(),
    val filteredPesagens: List<Pesagem> = emptyList(),
    val dateFilterType: String? = null,
    val dateFilterValue: Date? = null,
    val weightFilterType: String? = null,
    val weightValue: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PesagemHistoryViewModel @Inject constructor(
    private val pesagemRepository: PesagemRepository,
    private val cowRepository: CowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PesagemHistoryUiState())
    val uiState: StateFlow<PesagemHistoryUiState> = _uiState.asStateFlow()

    private var cowId: Long? = null

    fun init(cowId: Long) {
        this.cowId = cowId
        loadPesagens()
    }

    private fun loadPesagens() {
        val currentCowId = cowId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                pesagemRepository.getFilteredPesagens(
                    cowId = currentCowId,
                    dateFilterType = _uiState.value.dateFilterType,
                    dateValue = _uiState.value.dateFilterValue,
                    weightFilterType = _uiState.value.weightFilterType,
                    weightValue = _uiState.value.weightValue.toDoubleOrNull()
                ).collect { pesagens ->
                    _uiState.update {
                        it.copy(
                            pesagens = pesagens,
                            filteredPesagens = pesagens,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar pesagens: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateDateFilterType(type: String?) {
        _uiState.update { it.copy(dateFilterType = type) }
        loadPesagens()
    }

    fun updateDateFilterValue(date: Date?) {
        _uiState.update { it.copy(dateFilterValue = date) }
        loadPesagens()
    }

    fun updateWeightFilterType(type: String?) {
        _uiState.update { it.copy(weightFilterType = type) }
        loadPesagens()
    }

    fun updateWeightValue(value: String) {
        _uiState.update { it.copy(weightValue = value) }
        loadPesagens()
    }

    fun deletePesagem(pesagem: Pesagem) {
        viewModelScope.launch {
            try {
                pesagemRepository.deletePesagem(pesagem)
                // Recalcular o peso da vaca após excluir
                updateCowWeight(pesagem.cowId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao excluir pesagem: ${e.message}")
                }
            }
        }
    }

    private suspend fun updateCowWeight(cowId: Long) {
        try {
            // Buscar todas as pesagens da vaca
            val pesagens = pesagemRepository.getPesagensByCowId(cowId).first()
            
            // Encontrar a pesagem mais recente (por data, e se empatar, por ID)
            val latestPesagem = pesagens.maxWithOrNull(
                compareBy<Pesagem> { it.date }
                    .thenBy { it.id }
            )
            
            // Atualizar o peso da vaca
            val cow = cowRepository.getCowById(cowId)
            cow?.let {
                val updatedCow = if (latestPesagem != null) {
                    it.copy(pesoKg = latestPesagem.weightKg)
                } else {
                    // Se não houver mais pesagens, manter o peso atual ou definir como 0
                    it.copy(pesoKg = 0.0)
                }
                cowRepository.updateCow(updatedCow)
            }
        } catch (e: Exception) {
            // Falha ao atualizar peso, mas não quebra o fluxo
        }
    }
}

