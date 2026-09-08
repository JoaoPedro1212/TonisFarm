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

data class FinanceChartsUiState(
    val monthlySummaries: List<MonthlySummary> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FinanceChartsViewModel @Inject constructor(
    private val transactionRepository: FinancialTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceChartsUiState())
    val uiState: StateFlow<FinanceChartsUiState> = _uiState.asStateFlow()

    fun loadChartData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            val endDate = calendar.time
            calendar.add(Calendar.MONTH, -12)
            val startDate = calendar.time

            transactionRepository.getTransactionsByDateRange(startDate, endDate)
                .first()
                .let { transactions ->
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

                    // Gerar dados mockados para meses faltantes (apenas para visualização)
                    val summariesWithMock = generateMockDataForMissingMonths(monthlyMap)

                    _uiState.update {
                        it.copy(
                            monthlySummaries = summariesWithMock,
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * Gera dados para os últimos 12 meses usando apenas dados reais.
     * Meses sem dados reais terão valores zero.
     */
    private fun generateMockDataForMissingMonths(
        realData: Map<String, Pair<Double, Double>>
    ): List<MonthlySummary> {
        val calendar = Calendar.getInstance()
        val allMonths = mutableListOf<MonthlySummary>()
        val dateFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val monthNameFormat = SimpleDateFormat("MMM/yy", Locale("pt", "BR"))

        // Gerar lista dos últimos 12 meses (incluindo o mês atual)
        for (i in 11 downTo 0) {
            val monthCalendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, -i)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val monthKey = dateFormat.format(monthCalendar.time)
            val monthName = monthNameFormat.format(monthCalendar.time).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() 
            }

            // Usar apenas dados reais, ou zero se não houver dados
            val (receitas, despesas) = realData[monthKey] ?: (0.0 to 0.0)

            allMonths.add(
                MonthlySummary(
                    month = monthName,
                    receitas = receitas,
                    despesas = despesas,
                    lucro = receitas - despesas
                )
            )
        }

        // Ordenar do mais recente para o mais antigo (reverter a ordem)
        return allMonths.reversed()
    }
}

