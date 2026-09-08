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

data class IncomeListUiState(
    val incomes: List<FinancialTransaction> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class IncomeListViewModel @Inject constructor(
    private val transactionRepository: FinancialTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomeListUiState())
    val uiState: StateFlow<IncomeListUiState> = _uiState.asStateFlow()

    fun loadCurrentMonthIncomes() {
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
                .filter { it.tipo == TipoTransacao.RECEITA }
                .sortedByDescending { it.data }
                .let { incomes ->
                    _uiState.update {
                        it.copy(
                            incomes = incomes,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun addIncome(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                if (transaction.id > 0) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                loadCurrentMonthIncomes()
            } catch (e: Exception) {
                // Tratar erro se necessário
            }
        }
    }

    fun deleteIncome(transaction: FinancialTransaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                loadCurrentMonthIncomes()
            } catch (e: Exception) {
                // Tratar erro se necessário
            }
        }
    }
}

