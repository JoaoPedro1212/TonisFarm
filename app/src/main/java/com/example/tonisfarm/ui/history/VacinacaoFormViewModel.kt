package com.example.tonisfarm.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.VacinacaoRepository
import com.example.tonisfarm.domain.model.Vacinacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class VacinacaoFormUiState(
    val vaccineName: String = "",
    val customVaccineName: String = "",
    val date: Date? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class VacinacaoFormViewModel @Inject constructor(
    private val vacinacaoRepository: VacinacaoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VacinacaoFormUiState())
    val uiState: StateFlow<VacinacaoFormUiState> = _uiState.asStateFlow()

    private var editingVacinacaoId: Long? = null
    private var cowId: Long? = null

    fun init(cowId: Long, vacinacaoId: Long? = null) {
        this.cowId = cowId
        this.editingVacinacaoId = vacinacaoId
        if (vacinacaoId != null) {
            loadVacinacao(vacinacaoId)
        }
    }

    private fun loadVacinacao(vacinacaoId: Long) {
        viewModelScope.launch {
            val vacinacao = vacinacaoRepository.getVacinacaoById(vacinacaoId)
            vacinacao?.let {
                _uiState.update { state ->
                    state.copy(
                        vaccineName = if (it.vaccineName in getVaccineOptions()) it.vaccineName else "Outra",
                        customVaccineName = if (it.vaccineName in getVaccineOptions()) "" else it.vaccineName,
                        date = it.date
                    )
                }
            }
        }
    }

    fun updateVaccineName(name: String) {
        _uiState.update { it.copy(vaccineName = name, customVaccineName = if (name == "Outra") it.customVaccineName else "") }
    }

    fun updateCustomVaccineName(name: String) {
        _uiState.update { it.copy(customVaccineName = name) }
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(date = date) }
    }

    fun getVaccineOptions(): List<String> {
        return listOf(
            "Todas",
            "Aftosa (Febre Aftosa)",
            "Brucelose",
            "Clostridioses (Carbúnculo sintomático, Manqueira etc.)",
            "Raiva",
            "IBR/BVD",
            "Leptospirose",
            "Rinotraqueíte Infecciosa Bovina",
            "Outra"
        )
    }

    private fun validateForm(): String? {
        val state = _uiState.value
        if (state.vaccineName.isEmpty() || state.vaccineName == "Todas") {
            return "Selecione uma vacina"
        }
        val finalVaccineName = if (state.vaccineName == "Outra") {
            if (state.customVaccineName.isBlank()) {
                return "Digite o nome da vacina"
            }
            state.customVaccineName
        } else {
            state.vaccineName
        }
        if (state.date == null) {
            return "Data é obrigatória"
        }
        return null
    }

    fun saveVacinacao(onSuccess: () -> Unit) {
        val error = validateForm()
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val state = _uiState.value
                val finalVaccineName = if (state.vaccineName == "Outra") {
                    state.customVaccineName
                } else {
                    state.vaccineName
                }

                val vacinacao = Vacinacao(
                    id = editingVacinacaoId ?: 0,
                    cowId = cowId!!,
                    vaccineName = finalVaccineName,
                    date = state.date!!
                )

                if (editingVacinacaoId != null) {
                    vacinacaoRepository.updateVacinacao(vacinacao)
                } else {
                    vacinacaoRepository.insertVacinacao(vacinacao)
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

