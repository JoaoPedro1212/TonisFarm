package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.PartoRepository
import com.example.tonisfarm.domain.model.Parto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PartoFormUiState(
    val calvesCount: String = "",
    val date: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class PartoFormViewModel @Inject constructor(
    private val partoRepository: PartoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartoFormUiState())
    val uiState: StateFlow<PartoFormUiState> = _uiState.asStateFlow()

    private var editingPartoId: Long? = null
    private var cowId: Long? = null

    fun init(cowId: Long, partoId: Long? = null) {
        this.cowId = cowId
        this.editingPartoId = partoId
        if (partoId != null) {
            loadParto(partoId)
        }
    }

    private fun loadParto(partoId: Long) {
        viewModelScope.launch {
            val parto = partoRepository.getPartoById(partoId)
            parto?.let {
                _uiState.update { state ->
                    state.copy(
                        // Se a quantidade não foi informada, deixar campo vazio
                        calvesCount = if (it.isCalvesCountInformed) it.calvesCount.toString() else "",
                        date = it.date
                    )
                }
            }
        }
    }

    fun updateCalvesCount(count: String) {
        _uiState.update { it.copy(calvesCount = count) }
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    private fun validateForm(): String? {
        val state = _uiState.value
        // Quantidade pode ficar vazia (será "Não informado")
        if (state.calvesCount.isNotBlank()) {
            val count = state.calvesCount.toIntOrNull()
            if (count == null || count < 0) {
                return "Quantidade deve ser um número válido maior ou igual a zero"
            }
        }
        if (state.date == null) {
            return "Data é obrigatória"
        }
        return null
    }

    fun saveParto(onSuccess: () -> Unit) {
        val error = validateForm()
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val state = _uiState.value

                // Se a quantidade estiver vazia, usar valor "Não informado"
                val calvesCount = if (state.calvesCount.isBlank()) {
                    Parto.CALVES_COUNT_NOT_INFORMED
                } else {
                    state.calvesCount.toInt()
                }
                
                val parto = Parto(
                    id = editingPartoId ?: 0,
                    cowId = cowId!!,
                    calvesCount = calvesCount,
                    date = state.date!!
                )

                if (editingPartoId != null) {
                    partoRepository.updateParto(parto)
                } else {
                    partoRepository.insertParto(parto)
                }

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao salvar: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

