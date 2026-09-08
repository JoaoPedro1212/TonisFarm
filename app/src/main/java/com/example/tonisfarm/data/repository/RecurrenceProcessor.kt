package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.FinancialTransactionDao
import com.example.tonisfarm.domain.model.FinancialTransaction
import com.example.tonisfarm.domain.model.RecurrenceType
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class RecurrenceProcessor @Inject constructor(
    private val transactionDao: FinancialTransactionDao
) {
    suspend fun processRecurrences() {
        val today = Date()
        val todayTime = today.time
        
        // Buscar todas as transações recorrentes com nextOccurrenceDate não nulo
        val recurringTransactions = transactionDao.getRecurringTransactions()
            .map { FinancialTransaction.fromEntity(it) }
        
        for (originalTransaction in recurringTransactions) {
            val initialNextDate = originalTransaction.nextOccurrenceDate
            if (initialNextDate == null) continue
            
            val nextOccurrenceTime = initialNextDate.time
            
            // Processar todas as ocorrências que já deveriam ter sido criadas
            var currentNextDate: Date = initialNextDate
            var currentNextTime = currentNextDate.time
            
            while (currentNextTime <= todayTime) {
                // Criar nova transação filha (ocorrência)
                val newTransaction = FinancialTransaction(
                    id = 0, // Nova transação
                    tipo = originalTransaction.tipo,
                    categoria = originalTransaction.categoria,
                    valor = originalTransaction.valor,
                    data = currentNextDate,
                    descricao = originalTransaction.descricao,
                    recurrenceType = RecurrenceType.NONE, // A cópia não é recorrente
                    nextOccurrenceDate = null // A cópia não tem próxima ocorrência
                )
                
                // Inserir a ocorrência
                transactionDao.insertTransaction(newTransaction.toEntity())
                
                // Atualizar nextOccurrenceDate da transação original
                val calendar = Calendar.getInstance()
                calendar.time = currentNextDate
                
                when (originalTransaction.recurrenceType) {
                    RecurrenceType.MONTHLY -> {
                        calendar.add(Calendar.MONTH, 1)
                    }
                    RecurrenceType.YEARLY -> {
                        calendar.add(Calendar.YEAR, 1)
                    }
                    RecurrenceType.NONE -> {
                        break // Não deveria acontecer, mas por segurança
                    }
                }
                
                currentNextDate = calendar.time
                currentNextTime = currentNextDate.time
            }
            
            // Atualizar a transação original com o novo nextOccurrenceDate
            if (currentNextTime > nextOccurrenceTime) {
                val updatedTransaction = originalTransaction.copy(
                    nextOccurrenceDate = currentNextDate
                )
                transactionDao.updateTransaction(updatedTransaction.toEntity())
            }
        }
    }
}

