package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.Vermifugacao
import kotlinx.coroutines.flow.Flow

interface VermifugacaoRepository {
    fun getVermifugacoesByCowId(cowId: Long): Flow<List<Vermifugacao>>
    suspend fun getLastVermifugacaoByCowId(cowId: Long): Vermifugacao?
    suspend fun getVermifugacaoById(id: Long): Vermifugacao?
    fun getFilteredVermifugacoes(
        cowId: Long,
        drugName: String? = null,
        dateFilterType: String? = null,
        dateValue: java.util.Date? = null
    ): Flow<List<Vermifugacao>>
    suspend fun insertVermifugacao(vermifugacao: Vermifugacao): Long
    suspend fun updateVermifugacao(vermifugacao: Vermifugacao)
    suspend fun deleteVermifugacao(vermifugacao: Vermifugacao)
}

