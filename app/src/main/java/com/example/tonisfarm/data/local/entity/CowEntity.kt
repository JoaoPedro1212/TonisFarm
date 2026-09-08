package com.example.tonisfarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cows")
data class CowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identificacao: String,
    val nome: String? = null,
    val dataNascimento: Long, // Timestamp
    val pesoKg: Double,
    val raca: String,
    val estaPrenha: Boolean = false,
    val mesesPrenha: Int? = null,
    val dataInicioPrenhez: Long? = null, // Timestamp - nova coluna
    val observacoes: String? = null,
    val ultimaVacinacaoManual: Long? = null, // Timestamp - override manual
    val ultimaVermifugacaoManual: Long? = null, // Timestamp - override manual
    val ultimaPesagemManual: Long? = null, // Timestamp - override manual
    val ultimoPartoManual: Long? = null // Timestamp - override manual
)

