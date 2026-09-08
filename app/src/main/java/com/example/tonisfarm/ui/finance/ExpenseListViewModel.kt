package com.example.tonisfarm.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonisfarm.data.repository.FinancialTransactionRepository
import com.example.tonisfarm.domain.model.FinancialTransaction
import com.example.tonisfarm.domain.model.TipoTransacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ExpenseListUiState(
    val expenses: List<FinancialTransaction> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val transactionRepository: FinancialTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    fun loadCurrentMonthExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            val currentMonthStart = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val currentMonthEnd = Calendar.getInstance().time

            transactionRepository.getTransactionsByDateRange(currentMonthStart, currentMonthEnd)
                .first()
                .filter { it.tipo == TipoTransacao.DESPESA }
                .sortedByDescending { it.data }
                .let { expenses ->
                    _uiState.update {
                        it.copy(
                            expenses = expenses,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun addExpense(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                if (transaction.id > 0) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                loadCurrentMonthExpenses()
            } catch (e: Exception) {
                // Tratar erro se necessário
            }
        }
    }

    fun deleteExpense(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                loadCurrentMonthExpenses()
            } catch (e: Exception) {
                // Tratar erro se necessário
            }
        }
    }
}

