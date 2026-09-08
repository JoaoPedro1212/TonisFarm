package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.VacinacaoEntity
import java.util.Date

data class Vacinacao(
    val id: Long = 0,
    val cowId: Long,
    val vaccineName: String,
    val date: Date
) {
    fun toEntity(): VacinacaoEntity {
        return VacinacaoEntity(
            id = id,
            cowId = cowId,
            vaccineName = vaccineName,
            date = date.time
        )
    }

    companion object {
        fun fromEntity(entity: VacinacaoEntity): Vacinacao {
            return Vacinacao(
                id = entity.id,
                cowId = entity.cowId,
                vaccineName = entity.vaccineName,
                date = Date(entity.date)
            )
        }
    }
}

