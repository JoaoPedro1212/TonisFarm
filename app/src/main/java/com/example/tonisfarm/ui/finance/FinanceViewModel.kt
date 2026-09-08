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

data class FinanceUiState(
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val currentMonthReceitas: Double = 0.0,
    val currentMonthDespesas: Double = 0.0,
    val currentMonthLucro: Double = 0.0,
    val currentMonthTransactions: List<com.example.tonisfarm.domain.model.FinancialTransaction> = emptyList(),
    val despesasByCategoria: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val transactionRepository: FinancialTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadFinanceData()
    }

    private fun loadFinanceData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Processar recorrências ANTES de começar a coletar o Flow
            // Isso garante que os dados estejam atualizados quando o Flow começar a emitir
            transactionRepository.processRecurrences()

            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.MONTH, -12)
            val startDate = calendar.time

            // Calcular mês atual para transações e categorias
            val currentMonthStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val currentMonthEnd = Calendar.getInstance().time

            // Combinar múltiplos Flows para atualização reativa
            combine(
                transactionRepository.getTransactionsByDateRange(startDate, endDate),
                transactionRepository.getCurrentMonthSummary(),
                transactionRepository.getTransactionsByDateRange(currentMonthStart, currentMonthEnd),
                transactionRepository.getDespesasByCategoriaFlow(currentMonthStart, currentMonthEnd)
            ) { allTransactions, currentMonthSummary, currentMonthTransactions, categorias ->
                // Processar transações dos últimos 12 meses para histórico
                val monthlyMap = mutableMapOf<String, Pair<Double, Double>>()

                allTransactions.forEach { transaction ->
                    val monthKey = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(transaction.data)
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
                }.sortedBy { it.month }

                _uiState.update {
                    it.copy(
                        monthlySummaries = summaries,
                        currentMonthReceitas = currentMonthSummary.receitas,
                        currentMonthDespesas = currentMonthSummary.despesas,
                        currentMonthLucro = currentMonthSummary.lucro,
                        currentMonthTransactions = currentMonthTransactions.sortedByDescending { it.data },
                        despesasByCategoria = categorias,
                        isLoading = false
                    )
                }
            }.collect { }
        }
    }

    fun refresh() {
        loadFinanceData()
    }
}

