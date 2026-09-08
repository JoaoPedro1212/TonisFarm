package com.example.tonisfarm.data.local.dao

import androidx.room.*
import com.example.tonisfarm.data.local.entity.FinancialTransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategoriaTotal(
    val categoria: String,
    val total: Double
)

@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions WHERE data >= :startTime AND data <= :endTime ORDER BY data DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions ORDER BY data DESC")
    fun getAllTransactions(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): FinancialTransactionEntity?

    @Query("SELECT SUM(valor) FROM financial_transactions WHERE tipo = 'RECEITA' AND data >= :startTime AND data <= :endTime")
    fun getTotalReceitas(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(valor) FROM financial_transactions WHERE tipo = 'DESPESA' AND data >= :startTime AND data <= :endTime")
    fun getTotalDespesas(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT categoria, SUM(valor) as total FROM financial_transactions WHERE tipo = 'DESPESA' AND data >= :startTime AND data <= :endTime GROUP BY categoria")
    suspend fun getDespesasByCategoria(startTime: Long, endTime: Long): List<CategoriaTotal>
    
    // Versão reativa que observa mudanças nas transações
    @Query("SELECT categoria, SUM(valor) as total FROM financial_transactions WHERE tipo = 'DESPESA' AND data >= :startTime AND data <= :endTime GROUP BY categoria")
    fun getDespesasByCategoriaFlow(startTime: Long, endTime: Long): Flow<List<CategoriaTotal>>

    @Query("SELECT SUM(valor) FROM financial_transactions WHERE tipo = 'RECEITA' AND data >= :startTime AND data <= :endTime")
    suspend fun getTotalReceitasSync(startTime: Long, endTime: Long): Double?

    @Query("SELECT SUM(valor) FROM financial_transactions WHERE tipo = 'DESPESA' AND data >= :startTime AND data <= :endTime")
    suspend fun getTotalDespesasSync(startTime: Long, endTime: Long): Double?

    @Query("SELECT * FROM financial_transactions WHERE recurrenceType != 'NONE' AND nextOccurrenceDate IS NOT NULL")
    suspend fun getRecurringTransactions(): List<FinancialTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: FinancialTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransactionEntity)
}

