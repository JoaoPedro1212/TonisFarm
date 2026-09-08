package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.EventoEntity
import java.util.Date

data class Evento(
    val id: Long = 0,
    val tipoEvento: TipoEvento,
    val dataEvento: Date,
    val horaEvento: Date? = null, // Horário do evento (opcional) - apenas hora/minuto
    val descricao: String?,
    val dataAlerta: Date?, // Opcional
    val vaccineType: String? = null, // Tipo de vacina (apenas para eventos de VACINACAO)
    val dewormingType: String? = null, // Tipo de vermífugo (apenas para eventos de VERMIFUGACAO)
    val weightKg: Double? = null, // Peso em kg (apenas para eventos de PESAGEM)
    val calvesCount: Int? = null, // Quantidade de bezerros (apenas para eventos de PARTO)
    val isCompleted: Boolean = false // Indica se o evento já foi concluído/processado
) {
    fun toEntity(): EventoEntity {
        // Converter horaEvento para milissegundos desde meia-noite (0-86399999)
        val horaEventoMs = horaEvento?.let { hora ->
            val calendar = java.util.Calendar.getInstance()
            calendar.time = hora
            val hours = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(java.util.Calendar.MINUTE)
            (hours * 3600000L + minutes * 60000L)
        }
        
        return EventoEntity(
            id = id,
            tipoEvento = tipoEvento.name,
            dataEvento = dataEvento.time,
            horaEvento = horaEventoMs,
            descricao = descricao,
            dataAlerta = dataAlerta?.time,
            vaccineType = vaccineType,
            dewormingType = dewormingType,
            weightKg = weightKg,
            calvesCount = calvesCount,
            isCompleted = isCompleted
        )
    }

    companion object {
        fun fromEntity(entity: EventoEntity): Evento {
            // Converter horaEvento de milissegundos desde meia-noite para Date
            val horaEventoDate = entity.horaEvento?.let { msDesdeMeiaNoite ->
                val calendar = java.util.Calendar.getInstance()
                calendar.time = Date(entity.dataEvento) // Usar data do evento como base
                val hours = (msDesdeMeiaNoite / 3600000).toInt()
                val minutes = ((msDesdeMeiaNoite % 3600000) / 60000).toInt()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hours)
                calendar.set(java.util.Calendar.MINUTE, minutes)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                calendar.time
            }
            
            return Evento(
                id = entity.id,
                tipoEvento = TipoEvento.valueOf(entity.tipoEvento),
                dataEvento = Date(entity.dataEvento),
                horaEvento = horaEventoDate,
                descricao = entity.descricao,
                dataAlerta = entity.dataAlerta?.let { Date(it) },
                vaccineType = entity.vaccineType,
                dewormingType = entity.dewormingType,
                weightKg = entity.weightKg,
                calvesCount = entity.calvesCount,
                isCompleted = entity.isCompleted
            )
        }
    }
}

enum class TipoEvento {
    PARTO,
    VACINACAO,
    VERMIFUGACAO,
    PESAGEM,
    OUTRO
}

