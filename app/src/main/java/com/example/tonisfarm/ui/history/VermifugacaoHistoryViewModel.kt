package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.VermifugacaoRepository
import com.example.tonisfarm.domain.model.Vermifugacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class VermifugacaoHistoryUiState(
    val vermifugacoes: List<Vermifugacao> = emptyList(),
    val filteredVermifugacoes: List<Vermifugacao> = emptyList(),
    val selectedDrugName: String = "Todas",
    val dateFilterType: String? = null,
    val dateFilterValue: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VermifugacaoHistoryViewModel @Inject constructor(
    private val vermifugacaoRepository: VermifugacaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VermifugacaoHistoryUiState())
    val uiState: StateFlow<VermifugacaoHistoryUiState> = _uiState.asStateFlow()

    private var cowId: Long? = null

    fun init(cowId: Long) {
        this.cowId = cowId
        loadVermifugacoes()
    }

    private fun loadVermifugacoes() {
        val currentCowId = cowId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                vermifugacaoRepository.getFilteredVermifugacoes(
                    cowId = currentCowId,
                    drugName = if (_uiState.value.selectedDrugName == "Todas") null else _uiState.value.selectedDrugName,
                    dateFilterType = _uiState.value.dateFilterType,
                    dateValue = _uiState.value.dateFilterValue
                ).collect { vermifugacoes ->
                    _uiState.update {
                        it.copy(
                            vermifugacoes = vermifugacoes,
                            filteredVermifugacoes = vermifugacoes,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar vermifugações: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateDrugFilter(drugName: String) {
        _uiState.update { it.copy(selectedDrugName = drugName) }
        loadVermifugacoes()
    }

    fun updateDateFilterType(type: String?) {
        _uiState.update { it.copy(dateFilterType = type) }
        loadVermifugacoes()
    }

    fun updateDateFilterValue(date: Date?) {
        _uiState.update { it.copy(dateFilterValue = date) }
        loadVermifugacoes()
    }

    fun deleteVermifugacao(vermifugacao: Vermifugacao) {
        viewModelScope.launch {
            try {
                vermifugacaoRepository.deleteVermifugacao(vermifugacao)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao excluir vermifugação: ${e.message}")
                }
            }
        }
    }
}

