package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.Vacinacao
import kotlinx.coroutines.flow.Flow

interface VacinacaoRepository {
    fun getVacinacoesByCowId(cowId: Long): Flow<List<Vacinacao>>
    suspend fun getLastVacinacaoByCowId(cowId: Long): Vacinacao?
    suspend fun getVacinacaoById(id: Long): Vacinacao?
    fun getFilteredVacinacoes(
        cowId: Long,
        vaccineName: String? = null,
        dateFilterType: String? = null,
        dateValue: java.util.Date? = null
    ): Flow<List<Vacinacao>>
    suspend fun insertVacinacao(vacinacao: Vacinacao): Long
    suspend fun updateVacinacao(vacinacao: Vacinacao)
    suspend fun deleteVacinacao(vacinacao: Vacinacao)
}

