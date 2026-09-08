package com.example.tonisfarm.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.FinancialTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class FinanceHistoryUiState(
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FinanceHistoryViewModel @Inject constructor(
    private val transactionRepository: FinancialTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceHistoryUiState())
    val uiState: StateFlow<FinanceHistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.MONTH, -12)
            val startDate = calendar.time

            // Observar mudanças no Flow para atualizar automaticamente
            transactionRepository.getTransactionsByDateRange(startDate, endDate)
                .collect { transactions ->
                    val monthlyMap = mutableMapOf<String, Pair<Double, Double>>()

                    transactions.forEach { transaction ->
                        val monthKey = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(transaction.data)
                        val current = monthlyMap.getOrDefault(monthKey, 0.0 to 0.0)
                        when (transaction.tipo) {
                            com.example.tonisfarm.domain.model.TipoTransacao.RECEITA -> {
                                monthlyMap[monthKey] = (current.first + transaction.valor) to current.second
                            }
                            com.example.tonisfarm.domain.model.TipoTransacao.DESPESA -> {
                                monthlyMap[monthKey] = current.first to (current.second + transaction.valor)
                            }
                        }
                    }

                    val summaries = monthlyMap.map { (month, values) ->
                        MonthlySummary(
                            month = month,
                            receitas = values.first,
                            despesas = values.second,
                            lucro = values.first - values.second
                        )
                    }.sortedByDescending { it.month } // Mais recente primeiro

                    _uiState.update {
                        it.copy(
                            monthlySummaries = summaries,
                            isLoading = false
                        )
                    }
                }
        }
    }
}

