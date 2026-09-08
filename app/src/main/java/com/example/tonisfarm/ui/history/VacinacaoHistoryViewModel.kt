package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.VacinacaoRepository
import com.example.tonisfarm.domain.model.Vacinacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class VacinacaoHistoryUiState(
    val vacinacoes: List<Vacinacao> = emptyList(),
    val filteredVacinacoes: List<Vacinacao> = emptyList(),
    val selectedVaccineName: String = "Todas",
    val dateFilterType: String? = null,
    val dateFilterValue: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class VacinacaoHistoryViewModel @Inject constructor(
    private val vacinacaoRepository: VacinacaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacinacaoHistoryUiState())
    val uiState: StateFlow<VacinacaoHistoryUiState> = _uiState.asStateFlow()

    private var cowId: Long? = null

    fun init(cowId: Long) {
        this.cowId = cowId
        loadVacinacoes()
    }

    private fun loadVacinacoes() {
        val currentCowId = cowId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                vacinacaoRepository.getFilteredVacinacoes(
                    cowId = currentCowId,
                    vaccineName = if (_uiState.value.selectedVaccineName == "Todas") null else _uiState.value.selectedVaccineName,
                    dateFilterType = _uiState.value.dateFilterType,
                    dateValue = _uiState.value.dateFilterValue
                ).collect { vacinacoes ->
                    _uiState.update {
                        it.copy(
                            vacinacoes = vacinacoes,
                            filteredVacinacoes = vacinacoes,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar vacinações: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateVaccineFilter(vaccineName: String) {
        _uiState.update { it.copy(selectedVaccineName = vaccineName) }
        loadVacinacoes()
    }

    fun updateDateFilterType(type: String?) {
        _uiState.update { it.copy(dateFilterType = type) }
        loadVacinacoes()
    }

    fun updateDateFilterValue(date: Date?) {
        _uiState.update { it.copy(dateFilterValue = date) }
        loadVacinacoes()
    }

    fun deleteVacinacao(vacinacao: Vacinacao) {
        viewModelScope.launch {
            try {
                vacinacaoRepository.deleteVacinacao(vacinacao)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao excluir vacinação: ${e.message}")
                }
            }
        }
    }
}

