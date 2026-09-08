package com.example.tonisfarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_transactions")
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: String, // "DESPESA" ou "RECEITA"
    val categoria: String,
    val valor: Double,
    val data: Long, // Timestamp
    val descricao: String? = null,
    val recurrenceType: String = "NONE", // "NONE", "MONTHLY", "YEARLY"
    val nextOccurrenceDate: Long? = null // Timestamp da próxima ocorrência
)

