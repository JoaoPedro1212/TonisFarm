package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.FinancialTransactionDao
import com.example.tonisfarm.domain.model.FinancialTransaction
import com.example.tonisfarm.ui.finance.MonthlySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FinancialTransactionRepositoryImpl @Inject constructor(
    private val transactionDao: FinancialTransactionDao,
    private val recurrenceProcessor: RecurrenceProcessor
) : FinancialTransactionRepository {
    override fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<FinancialTransaction>> {
        return transactionDao.getTransactionsByDateRange(startDate.time, endDate.time).map { entities ->
            entities.map { FinancialTransaction.fromEntity(it) }
        }
    }

    override fun getAllTransactions(): Flow<List<FinancialTransaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { FinancialTransaction.fromEntity(it) }
        }
    }

    override suspend fun getTransactionById(id: Long): FinancialTransaction? {
        return transactionDao.getTransactionById(id)?.let { FinancialTransaction.fromEntity(it) }
    }

    override fun getTotalReceitas(startDate: Date, endDate: Date): Flow<Double> {
        return transactionDao.getTotalReceitas(startDate.time, endDate.time).map { it ?: 0.0 }
    }

    override fun getTotalDespesas(startDate: Date, endDate: Date): Flow<Double> {
        return transactionDao.getTotalDespesas(startDate.time, endDate.time).map { it ?: 0.0 }
    }

    override suspend fun getTotalReceitasSync(startDate: Date, endDate: Date): Double {
        return transactionDao.getTotalReceitasSync(startDate.time, endDate.time) ?: 0.0
    }

    override suspend fun getTotalDespesasSync(startDate: Date, endDate: Date): Double {
        return transactionDao.getTotalDespesasSync(startDate.time, endDate.time) ?: 0.0
    }

    override suspend fun getDespesasByCategoria(startDate: Date, endDate: Date): Map<String, Double> {
        val categorias = transactionDao.getDespesasByCategoria(startDate.time, endDate.time)
        return categorias.associate { it.categoria to it.total }
    }

    override fun getDespesasByCategoriaFlow(startDate: Date, endDate: Date): Flow<Map<String, Double>> {
        return transactionDao.getDespesasByCategoriaFlow(startDate.time, endDate.time)
            .map { categorias -> categorias.associate { it.categoria to it.total } }
    }

    override suspend fun insertTransaction(transaction: FinancialTransaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: FinancialTransaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: FinancialTransaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun processRecurrences() {
        recurrenceProcessor.processRecurrences()
    }

    override fun getCurrentMonthSummary(): Flow<MonthlySummary> {
        // Calcular início e fim do mês atual
        val calendar = Calendar.getInstance()
        val currentMonthStart = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val currentMonthEnd = Calendar.getInstance().time
        
        // Usar Flows reativos para receitas e despesas
        val receitasFlow = getTotalReceitas(currentMonthStart, currentMonthEnd)
        val despesasFlow = getTotalDespesas(currentMonthStart, currentMonthEnd)
        
        // Combinar os Flows para calcular o resumo do mês
        return combine(receitasFlow, despesasFlow) { receitas, despesas ->
            val monthKey = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(currentMonthStart)
            MonthlySummary(
                month = monthKey,
                receitas = receitas,
                despesas = despesas,
                lucro = receitas - despesas
            )
        }
    }
}

