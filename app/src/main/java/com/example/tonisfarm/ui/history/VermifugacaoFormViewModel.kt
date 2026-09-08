package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.VermifugacaoRepository
import com.example.tonisfarm.domain.model.Vermifugacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class VermifugacaoFormUiState(
    val drugName: String = "",
    val customDrugName: String = "",
    val date: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class VermifugacaoFormViewModel @Inject constructor(
    private val vermifugacaoRepository: VermifugacaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VermifugacaoFormUiState())
    val uiState: StateFlow<VermifugacaoFormUiState> = _uiState.asStateFlow()

    private var editingVermifugacaoId: Long? = null
    private var cowId: Long? = null

    fun init(cowId: Long, vermifugacaoId: Long? = null) {
        this.cowId = cowId
        this.editingVermifugacaoId = vermifugacaoId
        if (vermifugacaoId != null) {
            loadVermifugacao(vermifugacaoId)
        }
    }

    private fun loadVermifugacao(vermifugacaoId: Long) {
        viewModelScope.launch {
            val vermifugacao = vermifugacaoRepository.getVermifugacaoById(vermifugacaoId)
            vermifugacao?.let {
                _uiState.update { state ->
                    state.copy(
                        drugName = if (it.drugName in getDrugOptions()) it.drugName else "Outro",
                        customDrugName = if (it.drugName in getDrugOptions()) "" else it.drugName,
                        date = it.date
                    )
                }
            }
        }
    }

    fun updateDrugName(name: String) {
        _uiState.update { it.copy(drugName = name, customDrugName = if (name == "Outro") it.customDrugName else "") }
    }

    fun updateCustomDrugName(name: String) {
        _uiState.update { it.copy(customDrugName = name) }
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    fun getDrugOptions(): List<String> {
        return listOf(
            "Todas",
            "Ivermectina",
            "Albendazol",
            "Fenbendazol",
            "Levamisole",
            "Oxfendazol",
            "Outro"
        )
    }

    private fun validateForm(): String? {
        val state = _uiState.value
        if (state.drugName.isEmpty() || state.drugName == "Todas") {
            return "Selecione um vermífugo"
        }
        val finalDrugName = if (state.drugName == "Outro") {
            if (state.customDrugName.isBlank()) {
                return "Digite o nome do vermífugo"
            }
            state.customDrugName
        } else {
            state.drugName
        }
        if (state.date == null) {
            return "Data é obrigatória"
        }
        return null
    }

    fun saveVermifugacao(onSuccess: () -> Unit) {
        val error = validateForm()
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val state = _uiState.value
                val finalDrugName = if (state.drugName == "Outro") {
                    state.customDrugName
                } else {
                    state.drugName
                }

                val vermifugacao = Vermifugacao(
                    id = editingVermifugacaoId ?: 0,
                    cowId = cowId!!,
                    drugName = finalDrugName,
                    date = state.date!!
                )

                if (editingVermifugacaoId != null) {
                    vermifugacaoRepository.updateVermifugacao(vermifugacao)
                } else {
                    vermifugacaoRepository.insertVermifugacao(vermifugacao)
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

