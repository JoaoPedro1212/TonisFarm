package com.example.tonisfarm.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.data.repository.EventoRepository
import com.example.tonisfarm.domain.model.Cow
import com.example.tonisfarm.domain.model.Evento
import com.example.tonisfarm.domain.model.TipoEvento
import com.example.tonisfarm.util.CowFilters
import com.example.tonisfarm.util.filterCows
import com.example.tonisfarm.util.WeightFilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class EventFormUiState(
    val selectedCowIds: Set<Long> = emptySet(),
    val allCows: List<Cow> = emptyList(),
    val filteredCows: List<Cow> = emptyList(),
    val displayedCows: List<Cow> = emptyList(),
    val searchQuery: String = "",
    val selectedRace: String = "Todas",
    val weightFilterType: com.example.tonisfarm.util.WeightFilterType? = null,
    val weightValue: String = "",
    val tipoEvento: TipoEvento = TipoEvento.OUTRO,
    val dataEvento: Date? = null,
    val horaEvento: Date? = null, // Horário do evento (opcional)
    val descricao: String = "",
    val dataAlerta: Date? = null,
    val vaccineType: String = "", // Tipo de vacina (apenas para VACINACAO)
    val customVaccineType: String = "", // Nome customizado quando "Outra" é selecionada
    val dewormingType: String = "", // Tipo de vermífugo (apenas para VERMIFUGACAO)
    val customDewormingType: String = "", // Nome customizado quando "Outro" é selecionado
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class EventFormViewModel @Inject constructor(
    private val eventoRepository: EventoRepository,
    private val cowRepository: CowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    private var editingEventoId: Long? = null

    init {
        loadAllCows()
    }

    private fun loadAllCows() {
        viewModelScope.launch {
            cowRepository.getAllCows().first().let { cows ->
                // Ordenar por ID (maior = mais recente) para pegar as últimas 5
                val sortedCows = cows.sortedByDescending { it.id }
                _uiState.update { state ->
                    val filters = CowFilters(
                        searchQuery = state.searchQuery,
                        selectedRace = state.selectedRace,
                        weightFilterType = state.weightFilterType,
                        weightValue = state.weightValue.toDoubleOrNull()
                    )
                    val filtered = filterCows(sortedCows, filters)
                    val displayed = if (filters.hasActiveFilters()) {
                        filtered
                    } else {
                        filtered.take(5) // Primeiras 5 (mais recentes) quando sem filtros
                    }
                    state.copy(
                        allCows = sortedCows,
                        filteredCows = filtered,
                        displayedCows = displayed
                    )
                }
            }
        }
    }
    
    private fun updateFilteredCows() {
        val state = _uiState.value
        val filters = CowFilters(
            searchQuery = state.searchQuery,
            selectedRace = state.selectedRace,
            weightFilterType = state.weightFilterType,
            weightValue = state.weightValue.toDoubleOrNull()
        )
        val filtered = filterCows(state.allCows, filters)
        val displayed = if (filters.hasActiveFilters()) {
            filtered
        } else {
            filtered.take(5) // Primeiras 5 (mais recentes) quando sem filtros
        }
        _uiState.update { it.copy(filteredCows = filtered, displayedCows = displayed) }
    }

    fun setInitialCowId(cowId: Long) {
        _uiState.update { it.copy(selectedCowIds = setOf(cowId)) }
    }

    fun toggleCowSelection(cowId: Long) {
        _uiState.update { state ->
            val newSelection = if (state.selectedCowIds.contains(cowId)) {
                state.selectedCowIds - cowId
            } else {
                state.selectedCowIds + cowId
            }
            state.copy(selectedCowIds = newSelection)
        }
    }

    fun selectAllCows() {
        viewModelScope.launch {
            val state = _uiState.value
            val filters = CowFilters(
                searchQuery = state.searchQuery,
                selectedRace = state.selectedRace,
                weightFilterType = state.weightFilterType,
                weightValue = state.weightValue.toDoubleOrNull()
            )
            // Se houver filtros ativos, selecionar apenas as vacas filtradas
            // Caso contrário, selecionar todas as vacas
            val cowsToSelect = if (filters.hasActiveFilters()) {
                state.filteredCows
            } else {
                state.allCows
            }
            val allCowIds = cowsToSelect.map { it.id }.toSet()
            _uiState.update { it.copy(selectedCowIds = allCowIds) }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredCows()
    }
    
    fun updateSelectedRace(race: String) {
        _uiState.update { it.copy(selectedRace = race) }
        updateFilteredCows()
    }
    
    fun updateWeightFilterType(type: WeightFilterType?) {
        _uiState.update { it.copy(weightFilterType = type) }
        updateFilteredCows()
    }
    
    fun updateWeightValue(value: String) {
        _uiState.update { it.copy(weightValue = value) }
        updateFilteredCows()
    }

    fun loadEvento(eventoId: Long) {
        viewModelScope.launch {
            val evento = eventoRepository.getEventoById(eventoId)
            evento?.let {
                editingEventoId = it.id
                // Carregar vacas associadas ao evento
                val cowIds = eventoRepository.getCowIdsByEventoId(eventoId).first()
                _uiState.update { state ->
                    val vaccineType = it.vaccineType ?: ""
                    val dewormingType = it.dewormingType ?: ""
                    val vaccineOptions = getVaccineOptions()
                    val dewormingOptions = getDewormingOptions()
                    
                    val newState = state.copy(
                        selectedCowIds = cowIds.toSet(),
                        tipoEvento = it.tipoEvento,
                        dataEvento = it.dataEvento,
                        horaEvento = it.horaEvento,
                        descricao = it.descricao ?: "",
                        dataAlerta = it.dataAlerta,
                        vaccineType = if (vaccineType in vaccineOptions) vaccineType else if (vaccineType.isNotEmpty()) "Outra" else "",
                        customVaccineType = if (vaccineType !in vaccineOptions && vaccineType.isNotEmpty()) vaccineType else "",
                        dewormingType = if (dewormingType in dewormingOptions) dewormingType else if (dewormingType.isNotEmpty()) "Outro" else "",
                        customDewormingType = if (dewormingType !in dewormingOptions && dewormingType.isNotEmpty()) dewormingType else "",
                    )
                    // Aplicar filtros após atualizar o estado
                    val filters = CowFilters(
                        searchQuery = newState.searchQuery,
                        selectedRace = newState.selectedRace,
                        weightFilterType = newState.weightFilterType,
                        weightValue = newState.weightValue.toDoubleOrNull()
                    )
                    val filtered = filterCows(newState.allCows, filters)
                    val displayed = if (filters.hasActiveFilters()) {
                        filtered
                    } else {
                        filtered.take(5) // Primeiras 5 (mais recentes) quando sem filtros
                    }
                    newState.copy(filteredCows = filtered, displayedCows = displayed)
                }
            }
        }
    }

    fun updateTipoEvento(tipo: TipoEvento) {
        _uiState.update { it.copy(tipoEvento = tipo) }
    }

    fun updateDataEvento(date: Date) {
        _uiState.update { it.copy(dataEvento = date) }
    }

    fun updateHoraEvento(date: Date?) {
        _uiState.update { it.copy(horaEvento = date) }
    }

    fun updateDescricao(value: String) {
        _uiState.update { it.copy(descricao = value) }
    }

    fun updateDataAlerta(date: Date?) {
        _uiState.update { it.copy(dataAlerta = date) }
    }

    fun updateVaccineType(type: String) {
        _uiState.update { 
            it.copy(
                vaccineType = type,
                customVaccineType = if (type == "Outra") it.customVaccineType else ""
            )
        }
    }

    fun updateCustomVaccineType(type: String) {
        _uiState.update { it.copy(customVaccineType = type) }
    }

    fun updateDewormingType(type: String) {
        _uiState.update { 
            it.copy(
                dewormingType = type,
                customDewormingType = if (type == "Outro") it.customDewormingType else ""
            )
        }
    }

    fun updateCustomDewormingType(type: String) {
        _uiState.update { it.copy(customDewormingType = type) }
    }


    fun getVaccineOptions(): List<String> {
        return listOf(
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

    fun getDewormingOptions(): List<String> {
        return listOf(
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
        if (state.selectedCowIds.isEmpty()) {
            return "Selecione pelo menos uma vaca"
        }
        if (state.dataEvento == null) {
            return "Data do evento é obrigatória"
        }
        // dataAlerta agora é opcional
        return null
    }

    fun saveEvento(onSuccess: () -> Unit) {
        val error = validateForm()
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val state = _uiState.value

                // Determinar o tipo de vacina/vermífugo final
                val finalVaccineType = when (state.tipoEvento) {
                    TipoEvento.VACINACAO -> {
                        if (state.vaccineType == "Outra") {
                            state.customVaccineType.ifBlank { null }
                        } else {
                            state.vaccineType.ifBlank { null }
                        }
                    }
                    else -> null
                }
                
                val finalDewormingType = when (state.tipoEvento) {
                    TipoEvento.VERMIFUGACAO -> {
                        if (state.dewormingType == "Outro") {
                            state.customDewormingType.ifBlank { null }
                        } else {
                            state.dewormingType.ifBlank { null }
                        }
                    }
                    else -> null
                }

                // Peso e quantidade de bezerros não são preenchidos na criação do evento
                // Eles serão preenchidos automaticamente quando o evento for concluído

                val evento = Evento(
                    id = editingEventoId ?: 0,
                    tipoEvento = state.tipoEvento,
                    dataEvento = state.dataEvento!!,
                    horaEvento = state.horaEvento,
                    descricao = state.descricao.ifBlank { null },
                    dataAlerta = state.dataAlerta,
                    vaccineType = finalVaccineType,
                    dewormingType = finalDewormingType,
                    weightKg = null, // Será preenchido quando o evento for concluído
                    calvesCount = null, // Será preenchido quando o evento for concluído
                    isCompleted = false // Novos eventos sempre começam como não concluídos
                )

                if (editingEventoId != null) {
                    // Edição: atualizar evento e associações
                    eventoRepository.updateEvento(evento, state.selectedCowIds.toList())
                } else {
                    // Criação: criar apenas 1 evento e associar todas as vacas
                    eventoRepository.insertEvento(evento, state.selectedCowIds.toList())
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

