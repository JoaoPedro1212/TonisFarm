package com.example.tonisfarm.domain.model

import com.example.tonisfarm.data.local.entity.CowEntity
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

data class Cow(
    val id: Long = 0,
    val identificacao: String,
    val nome: String? = null,
    val dataNascimento: Date,
    val pesoKg: Double,
    val raca: String,
    val estaPrenha: Boolean = false,
    val mesesPrenha: Int? = null,
    val dataInicioPrenhez: Date? = null, // Nova propriedade
    val observacoes: String? = null,
    val ultimaVacinacaoManual: Date? = null, // Override manual
    val ultimaVermifugacaoManual: Date? = null, // Override manual
    val ultimaPesagemManual: Date? = null, // Override manual
    val ultimoPartoManual: Date? = null // Override manual
) {
    fun toEntity(): CowEntity {
        return CowEntity(
            id = id,
            identificacao = identificacao,
            nome = nome,
            dataNascimento = dataNascimento.time,
            pesoKg = pesoKg,
            raca = raca,
            estaPrenha = estaPrenha,
            mesesPrenha = mesesPrenha,
            dataInicioPrenhez = dataInicioPrenhez?.time,
            observacoes = observacoes,
            ultimaVacinacaoManual = ultimaVacinacaoManual?.time,
            ultimaVermifugacaoManual = ultimaVermifugacaoManual?.time,
            ultimaPesagemManual = ultimaPesagemManual?.time,
            ultimoPartoManual = ultimoPartoManual?.time
        )
    }

    companion object {
        fun fromEntity(entity: CowEntity): Cow {
            return Cow(
                id = entity.id,
                identificacao = entity.identificacao,
                nome = entity.nome,
                dataNascimento = Date(entity.dataNascimento),
                pesoKg = entity.pesoKg,
                raca = entity.raca,
                estaPrenha = entity.estaPrenha,
                mesesPrenha = entity.mesesPrenha,
                dataInicioPrenhez = entity.dataInicioPrenhez?.let { Date(it) },
                observacoes = entity.observacoes,
                ultimaVacinacaoManual = entity.ultimaVacinacaoManual?.let { Date(it) },
                ultimaVermifugacaoManual = entity.ultimaVermifugacaoManual?.let { Date(it) },
                ultimaPesagemManual = entity.ultimaPesagemManual?.let { Date(it) },
                ultimoPartoManual = entity.ultimoPartoManual?.let { Date(it) }
            )
        }
    }
    
    // Calcula meses de prenhez dinamicamente usando LocalDate e Period
    fun getMesesPrenhezAtual(): Int? {
        if (!estaPrenha || dataInicioPrenhez == null) return null
        return calcularMesesPrenhez(
            dataInicioPrenhez.toLocalDate(),
            LocalDate.now()
        )
    }
    
    // Converte Date para LocalDate
    private fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }
}

// Funções utilitárias para cálculos de data
fun calcularIdadeEmTexto(
    dataNascimento: LocalDate,
    hoje: LocalDate = LocalDate.now()
): String {
    val period = Period.between(dataNascimento, hoje)
    val anos = period.years.coerceAtLeast(0)
    val meses = period.months.coerceAtLeast(0)
    val dias = period.days.coerceAtLeast(0)
    return "${anos} Anos, ${meses} Meses e ${dias} Dias"
}

fun calcularMesesPrenhez(
    dataInicioPrenhez: LocalDate?,
    hoje: LocalDate = LocalDate.now()
): Int {
    if (dataInicioPrenhez == null || dataInicioPrenhez.isAfter(hoje)) return 0
    val period = Period.between(dataInicioPrenhez, hoje)
    val anos = period.years
    val meses = period.months
    // anos convertidos para meses + meses restantes
    return anos * 12 + meses
}

