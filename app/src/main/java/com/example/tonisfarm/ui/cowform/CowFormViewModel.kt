package com.example.tonisfarm.ui.cowform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.domain.model.Cow
import com.example.tonisfarm.domain.model.calcularMesesPrenhez
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

data class CowFormUiState(
    val identificacao: String = "",
    val nome: String = "",
    val dataNascimento: Date? = null,
    val pesoKg: String = "",
    val raca: String = "",
    val estaPrenha: Boolean = false,
    val mesesPrenha: String = "",
    val dataInicioPrenhez: Date? = null,
    val observacoes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class CowFormViewModel @Inject constructor(
    private val cowRepository: CowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CowFormUiState())
    val uiState: StateFlow<CowFormUiState> = _uiState.asStateFlow()

    private var editingCowId: Long? = null

    fun loadCow(cowId: Long) {
        viewModelScope.launch {
            val cow = cowRepository.getCowById(cowId)
            cow?.let {
                editingCowId = it.id
                _uiState.update { state ->
                    state.copy(
                        identificacao = it.identificacao,
                        nome = it.nome ?: "",
                        dataNascimento = it.dataNascimento,
                        pesoKg = it.pesoKg.toString(),
                        raca = it.raca,
                        estaPrenha = it.estaPrenha,
                        mesesPrenha = it.mesesPrenha?.toString() ?: "",
                        dataInicioPrenhez = it.dataInicioPrenhez,
                        observacoes = it.observacoes ?: ""
                    )
                }
            }
        }
    }

    fun updateIdentificacao(value: String) {
        _uiState.update { it.copy(identificacao = value) }
    }

    fun updateNome(value: String) {
        _uiState.update { it.copy(nome = value) }
    }

    fun updateDataNascimento(date: Date) {
        _uiState.update { it.copy(dataNascimento = date) }
    }

    fun updatePesoKg(value: String) {
        _uiState.update { it.copy(pesoKg = value) }
    }

    fun updateRaca(value: String) {
        _uiState.update { it.copy(raca = value) }
    }

    fun updateEstaPrenha(value: Boolean) {
        _uiState.update { it.copy(estaPrenha = value) }
    }

    fun updateMesesPrenha(value: String) {
        _uiState.update { it.copy(mesesPrenha = value) }
    }

    fun updateDataInicioPrenhez(date: Date) {
        _uiState.update { it.copy(dataInicioPrenhez = date) }
    }

    fun updateObservacoes(value: String) {
        _uiState.update { it.copy(observacoes = value) }
    }

    private fun validateForm(): String? {
        val state = _uiState.value
        if (state.identificacao.isBlank()) {
            return "Identificação é obrigatória"
        }
        if (state.dataNascimento == null) {
            return "Data de nascimento é obrigatória"
        }
        val peso = state.pesoKg.toDoubleOrNull()
        if (peso == null || peso <= 0) {
            return "Peso deve ser um número válido e positivo"
        }
        val validRaces = listOf("Nelore", "Angus", "Girolando", "Holandês", "Guzerá")
        if (state.raca.isBlank() || !validRaces.contains(state.raca)) {
            return "Raça é obrigatória e deve ser uma das opções disponíveis"
        }
        if (state.estaPrenha && state.dataInicioPrenhez == null) {
            return "Data de início da prenhez é obrigatória quando a vaca está prenha"
        }
        return null
    }

    fun saveCow(onSuccess: () -> Unit) {
        val error = validateForm()
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val state = _uiState.value
                val peso = state.pesoKg.toDouble()
                // Calcular meses de prenhez baseado na data de início usando LocalDate e Period
                val mesesPrenha = if (state.estaPrenha && state.dataInicioPrenhez != null) {
                    val dataInicioLocalDate = state.dataInicioPrenhez.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    calcularMesesPrenhez(dataInicioLocalDate, LocalDate.now())
                } else {
                    null
                }

                val cow = Cow(
                    id = editingCowId ?: 0,
                    identificacao = state.identificacao,
                    nome = state.nome.ifBlank { null },
                    dataNascimento = state.dataNascimento!!,
                    pesoKg = peso,
                    raca = state.raca,
                    estaPrenha = state.estaPrenha,
                    mesesPrenha = mesesPrenha,
                    dataInicioPrenhez = state.dataInicioPrenhez,
                    observacoes = state.observacoes.ifBlank { null }
                )

                if (editingCowId != null) {
                    cowRepository.updateCow(cow)
                } else {
                    cowRepository.insertCow(cow)
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

