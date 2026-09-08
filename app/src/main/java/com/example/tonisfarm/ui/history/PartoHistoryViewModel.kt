package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.PartoRepository
import com.example.tonisfarm.domain.model.Parto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class PartoHistoryUiState(
    val partos: List<Parto> = emptyList(),
    val filteredPartos: List<Parto> = emptyList(),
    val dateFilterType: String? = null,
    val dateFilterValue: Date? = null,
    val calvesCountFilter: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PartoHistoryViewModel @Inject constructor(
    private val partoRepository: PartoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartoHistoryUiState())
    val uiState: StateFlow<PartoHistoryUiState> = _uiState.asStateFlow()

    private var cowId: Long? = null

    fun init(cowId: Long) {
        this.cowId = cowId
        loadPartos()
    }

    private fun loadPartos() {
        val currentCowId = cowId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                partoRepository.getFilteredPartos(
                    cowId = currentCowId,
                    dateFilterType = _uiState.value.dateFilterType,
                    dateValue = _uiState.value.dateFilterValue,
                    calvesCountFilter = _uiState.value.calvesCountFilter?.toIntOrNull()
                ).collect { partos ->
                    _uiState.update {
                        it.copy(
                            partos = partos,
                            filteredPartos = partos,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao carregar partos: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateDateFilterType(type: String?) {
        _uiState.update { it.copy(dateFilterType = type) }
        loadPartos()
    }

    fun updateDateFilterValue(date: Date?) {
        _uiState.update { it.copy(dateFilterValue = date) }
        loadPartos()
    }

    fun updateCalvesCountFilter(count: String?) {
        _uiState.update { it.copy(calvesCountFilter = count) }
        loadPartos()
    }

    fun deleteParto(parto: Parto) {
        viewModelScope.launch {
            try {
                partoRepository.deleteParto(parto)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Erro ao excluir parto: ${e.message}")
                }
            }
        }
    }
}

