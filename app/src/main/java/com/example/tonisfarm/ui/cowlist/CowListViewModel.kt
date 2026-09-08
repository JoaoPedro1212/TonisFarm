package com.example.tonisfarm.ui.cowlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.CowRepository
import com.example.tonisfarm.domain.model.Cow
import com.example.tonisfarm.util.CowFilters
import com.example.tonisfarm.util.filterCows
import com.example.tonisfarm.util.WeightFilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CowListUiState(
    val cows: List<Cow> = emptyList(),
    val allCows: List<Cow> = emptyList(),
    val searchQuery: String = "",
    val selectedRace: String = "Todas",
    val weightFilterType: com.example.tonisfarm.util.WeightFilterType? = null,
    val weightValue: String = "",
    val isLoading: Boolean = false,
    val showPregnantOnly: Boolean = false
)

@HiltViewModel
class CowListViewModel @Inject constructor(
    private val cowRepository: CowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CowListUiState())
    val uiState: StateFlow<CowListUiState> = _uiState.asStateFlow()

    fun init(showPregnantOnly: Boolean = false) {
        _uiState.update { it.copy(showPregnantOnly = showPregnantOnly) }
        loadCows()
    }

    private fun loadCows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val state = _uiState.value
            val flow = when {
                state.showPregnantOnly -> cowRepository.getPregnantCows()
                else -> cowRepository.getAllCows()
            }

            flow.collect { baseCows ->
                _uiState.update { currentState ->
                    val filters = CowFilters(
                        searchQuery = currentState.searchQuery,
                        selectedRace = currentState.selectedRace,
                        weightFilterType = currentState.weightFilterType,
                        weightValue = currentState.weightValue.toDoubleOrNull()
                    )
                    val filtered = filterCows(baseCows, filters)
                    currentState.copy(
                        allCows = baseCows,
                        cows = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    fun updateSelectedRace(race: String) {
        _uiState.update { it.copy(selectedRace = race) }
        applyFilters()
    }
    
    fun updateWeightFilterType(type: WeightFilterType?) {
        _uiState.update { it.copy(weightFilterType = type) }
        applyFilters()
    }
    
    fun updateWeightValue(value: String) {
        _uiState.update { it.copy(weightValue = value) }
        applyFilters()
    }
    
    private fun applyFilters() {
        val state = _uiState.value
        val filters = CowFilters(
            searchQuery = state.searchQuery,
            selectedRace = state.selectedRace,
            weightFilterType = state.weightFilterType,
            weightValue = state.weightValue.toDoubleOrNull()
        )
        val filtered = filterCows(state.allCows, filters)
        _uiState.update { it.copy(cows = filtered) }
    }

    fun deleteCow(cow: Cow) {
        viewModelScope.launch {
            try {
                cowRepository.deleteCow(cow)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

