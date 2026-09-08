package com.example.tonisfarm.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.FinancialTransactionRepository
import com.example.tonisfarm.domain.model.FinancialTransaction
import com.example.tonisfarm.domain.model.TipoTransacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MonthDetailUiState(
    val month: String = "",
    val incomes: List<FinancialTransaction> = emptyList(),
    val expenses: List<FinancialTransaction> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MonthDetailViewModel @Inject constructor(
    private val transactionRepository: FinancialTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthDetailUiState())
    val uiState: StateFlow<MonthDetailUiState> = _uiState.asStateFlow()

    fun loadMonthDetail(monthKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Processar recorrências antes de carregar os dados
            transactionRepository.processRecurrences()

            try {
                // Parse do mês (formato MM/yyyy)
                val parts = monthKey.split("/")
                if (parts.size != 2) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val month = parts[0].toInt()
                val year = parts[1].toInt()

                // Calcular início e fim do mês
                val calendar = Calendar.getInstance()
                calendar.set(year, month - 1, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.time

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val monthEnd = calendar.time

                // Observar mudanças no Flow para atualizar automaticamente
                transactionRepository.getTransactionsByDateRange(monthStart, monthEnd)
                    .collect { transactions ->
                        val incomes = transactions
                            .filter { it.tipo == TipoTransacao.RECEITA }
                            .sortedByDescending { it.data }
                        
                        val expenses = transactions
                            .filter { it.tipo == TipoTransacao.DESPESA }
                            .sortedByDescending { it.data }

                        _uiState.update {
                            it.copy(
                                month = monthKey,
                                incomes = incomes,
                                expenses = expenses,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                if (transaction.id > 0) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                // Recarregar dados do mês
                val monthKey = _uiState.value.month
                if (monthKey.isNotEmpty()) {
                    loadMonthDetail(monthKey)
                }
            } catch (e: Exception) {
                // Tratar erro se necessário
            }
        }
    }

    fun deleteTransaction(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                // Recarregar dados do mês
                val monthKey = _uiState.value.month
                if (monthKey.isNotEmpty()) {
                    loadMonthDetail(monthKey)
                }
            } catch (e: Exception) {
                // Tratar erro se necessário
            }
        }
    }
}

