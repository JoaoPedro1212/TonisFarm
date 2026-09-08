package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.PesagemEntity
import java.util.Date

data class Pesagem(
    val id: Long = 0,
    val cowId: Long,
    val weightKg: Double, // -1.0 indica "Não informado"
    val date: Date
) {
    val isWeightInformed: Boolean
        get() = weightKg >= 0
    
    val displayWeight: String
        get() = if (isWeightInformed) {
            String.format("%.1f", weightKg)
        } else {
            "Não informado"
        }
    
    fun toEntity(): PesagemEntity {
        return PesagemEntity(
            id = id,
            cowId = cowId,
            weightKg = weightKg,
            date = date.time
        )
    }

    companion object {
        const val WEIGHT_NOT_INFORMED = -1.0
        
        fun fromEntity(entity: PesagemEntity): Pesagem {
            return Pesagem(
                id = entity.id,
                cowId = entity.cowId,
                weightKg = entity.weightKg,
                date = Date(entity.date)
            )
        }
    }
}

