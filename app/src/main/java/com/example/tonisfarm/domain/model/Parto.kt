package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.PartoEntity
import java.util.Date

data class Parto(
    val id: Long = 0,
    val cowId: Long,
    val calvesCount: Int, // -1 indica "Não informado"
    val date: Date
) {
    val isCalvesCountInformed: Boolean
        get() = calvesCount >= 0
    
    val displayCalvesCount: String
        get() = if (isCalvesCountInformed) {
            calvesCount.toString()
        } else {
            "Não informado"
        }
    
    fun toEntity(): PartoEntity {
        return PartoEntity(
            id = id,
            cowId = cowId,
            calvesCount = calvesCount,
            date = date.time
        )
    }

    companion object {
        const val CALVES_COUNT_NOT_INFORMED = -1
        
        fun fromEntity(entity: PartoEntity): Parto {
            return Parto(
                id = entity.id,
                cowId = entity.cowId,
                calvesCount = entity.calvesCount,
                date = Date(entity.date)
            )
        }
    }
}

