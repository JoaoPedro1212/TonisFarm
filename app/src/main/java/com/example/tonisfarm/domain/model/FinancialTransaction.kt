package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.FinancialTransactionEntity
import java.util.Date

data class FinancialTransaction(
    val id: Long = 0,
    val tipo: TipoTransacao,
    val categoria: String,
    val valor: Double,
    val data: Date,
    val descricao: String? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val nextOccurrenceDate: Date? = null
) {
    fun toEntity(): FinancialTransactionEntity {
        return FinancialTransactionEntity(
            id = id,
            tipo = tipo.name,
            categoria = categoria,
            valor = valor,
            data = data.time,
            descricao = descricao,
            recurrenceType = recurrenceType.name,
            nextOccurrenceDate = nextOccurrenceDate?.time
        )
    }

    companion object {
        fun fromEntity(entity: FinancialTransactionEntity): FinancialTransaction {
            return FinancialTransaction(
                id = entity.id,
                tipo = TipoTransacao.valueOf(entity.tipo),
                categoria = entity.categoria,
                valor = entity.valor,
                data = Date(entity.data),
                descricao = entity.descricao,
                recurrenceType = try {
                    RecurrenceType.valueOf(entity.recurrenceType)
                } catch (e: Exception) {
                    RecurrenceType.NONE
                },
                nextOccurrenceDate = entity.nextOccurrenceDate?.let { Date(it) }
            )
        }
    }
}

enum class TipoTransacao {
    DESPESA,
    RECEITA
}

