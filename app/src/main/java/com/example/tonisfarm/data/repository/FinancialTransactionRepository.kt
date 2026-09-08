package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.FinancialTransaction
import com.example.tonisfarm.ui.finance.MonthlySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.Date

interface FinancialTransactionRepository {
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<FinancialTransaction>>
    fun getAllTransactions(): Flow<List<FinancialTransaction>>
    suspend fun getTransactionById(id: Long): FinancialTransaction?
    fun getTotalReceitas(startDate: Date, endDate: Date): Flow<Double>
    fun getTotalDespesas(startDate: Date, endDate: Date): Flow<Double>
    suspend fun getTotalReceitasSync(startDate: Date, endDate: Date): Double
    suspend fun getTotalDespesasSync(startDate: Date, endDate: Date): Double
    suspend fun getDespesasByCategoria(startDate: Date, endDate: Date): Map<String, Double>
    fun getDespesasByCategoriaFlow(startDate: Date, endDate: Date): Flow<Map<String, Double>>
    suspend fun insertTransaction(transaction: FinancialTransaction): Long
    suspend fun updateTransaction(transaction: FinancialTransaction)
    suspend fun deleteTransaction(transaction: FinancialTransaction)
    suspend fun processRecurrences()
    
    /**
     * Retorna um Flow reativo com o resumo financeiro do mês atual.
     * Este Flow é atualizado automaticamente sempre que há mudanças nas transações.
     */
    fun getCurrentMonthSummary(): Flow<MonthlySummary>
}

