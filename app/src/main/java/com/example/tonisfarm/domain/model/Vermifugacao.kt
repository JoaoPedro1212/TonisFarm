package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.VermifugacaoEntity
import java.util.Date

data class Vermifugacao(
    val id: Long = 0,
    val cowId: Long,
    val drugName: String,
    val date: Date
) {
    fun toEntity(): VermifugacaoEntity {
        return VermifugacaoEntity(
            id = id,
            cowId = cowId,
            drugName = drugName,
            date = date.time
        )
    }

    companion object {
        fun fromEntity(entity: VermifugacaoEntity): Vermifugacao {
            return Vermifugacao(
                id = entity.id,
                cowId = entity.cowId,
                drugName = entity.drugName,
                date = Date(entity.date)
            )
        }
    }
}

