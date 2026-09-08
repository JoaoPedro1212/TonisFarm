package com.example.tonisfarm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "eventos"
)
data class EventoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoEvento: String, // "PARTO", "VACINACAO", "VERMIFUGACAO", "PESAGEM", etc.
    val dataEvento: Long, // Timestamp (apenas data, hora será 00:00)
    val horaEvento: Long? = null, // Timestamp do horário do evento (opcional) - milissegundos desde meia-noite
    val descricao: String?,
    val dataAlerta: Long?, // Timestamp para notificação (opcional)
    val vaccineType: String? = null, // Tipo de vacina (apenas para eventos de VACINACAO)
    val dewormingType: String? = null, // Tipo de vermífugo (apenas para eventos de VERMIFUGACAO)
    val weightKg: Double? = null, // Peso em kg (apenas para eventos de PESAGEM)
    val calvesCount: Int? = null, // Quantidade de bezerros (apenas para eventos de PARTO)
    val isCompleted: Boolean = false // Indica se o evento já foi concluído/processado
)

