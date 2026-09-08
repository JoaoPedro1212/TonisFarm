package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.PesagemRepository
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.domain.model.Pesagem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PesagemFormUiState(
    val weightKg: String = "",
    val date: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class PesagemFormViewModel @Inject constructor(
    private val pesagemRepository: PesagemRepository,
    private val cowRepository: CowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PesagemFormUiState())
    val uiState: StateFlow<PesagemFormUiState> = _uiState.asStateFlow()

    private var editingPesagemId: Long? = null
    private var cowId: Long? = null

    fun init(cowId: Long, pesagemId: Long? = null) {
        this.cowId = cowId
        this.editingPesagemId = pesagemId
        if (pesagemId != null) {
            loadPesagem(pesagemId)
        }
    }

    private fun loadPesagem(pesagemId: Long) {
        viewModelScope.launch {
            val pesagem = pesagemRepository.getPesagemById(pesagemId)
            pesagem?.let {
                _uiState.update { state ->
                    state.copy(
                        // Se o peso não foi informado, deixar campo vazio
                        weightKg = if (it.isWeightInformed) it.weightKg.toString() else "",
                        date = it.date
                    )
                }
            }
        }
    }

    fun updateWeight(weight: String) {
        _uiState.update { it.copy(weightKg = weight) }
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    private fun validateForm(): String? {
        val state = _uiState.value
        // Peso pode ficar vazio (será "Não informado")
        if (state.weightKg.isNotBlank()) {
            val weight = state.weightKg.toDoubleOrNull()
            if (weight == null || weight <= 0) {
                return "Peso deve ser um número válido maior que zero"
            }
        }
        if (state.date == null) {
            return "Data é obrigatória"
        }
        return null
    }

    fun savePesagem(onSuccess: () -> Unit) {
        val error = validateForm()
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val state = _uiState.value

                // Se o peso estiver vazio, usar valor "Não informado"
                val weight = if (state.weightKg.isBlank()) {
                    Pesagem.WEIGHT_NOT_INFORMED
                } else {
                    state.weightKg.toDouble()
                }
                
                val pesagem = Pesagem(
                    id = editingPesagemId ?: 0,
                    cowId = cowId!!,
                    weightKg = weight,
                    date = state.date!!
                )

                if (editingPesagemId != null) {
                    pesagemRepository.updatePesagem(pesagem)
                } else {
                    pesagemRepository.insertPesagem(pesagem)
                }

                // Atualizar o peso da vaca apenas se o peso foi informado
                if (pesagem.isWeightInformed) {
                    updateCowWeight(cowId!!)
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

    private suspend fun updateCowWeight(cowId: Long) {
        try {
            // Buscar todas as pesagens da vaca
            val pesagens = pesagemRepository.getPesagensByCowId(cowId).first()
            
            // Filtrar apenas pesagens com peso informado e encontrar a mais recente
            val latestPesagem = pesagens
                .filter { it.isWeightInformed }
                .maxWithOrNull(
                    compareBy<Pesagem> { it.date }
                        .thenBy { it.id }
                )
            
            // Atualizar o peso da vaca
            if (latestPesagem != null) {
                val cow = cowRepository.getCowById(cowId)
                cow?.let {
                    val updatedCow = it.copy(pesoKg = latestPesagem.weightKg)
                    cowRepository.updateCow(updatedCow)
                }
            }
        } catch (e: Exception) {
            // Falha ao atualizar peso, mas não quebra o fluxo
            // O erro já foi tratado no savePesagem
        }
    }
}

