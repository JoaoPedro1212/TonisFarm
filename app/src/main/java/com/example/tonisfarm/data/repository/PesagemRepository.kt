package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.Pesagem
import kotlinx.coroutines.flow.Flow

interface PesagemRepository {
    fun getPesagensByCowId(cowId: Long): Flow<List<Pesagem>>
    suspend fun getLastPesagemByCowId(cowId: Long): Pesagem?
    suspend fun getPesagemById(id: Long): Pesagem?
    fun getFilteredPesagens(
        cowId: Long,
        dateFilterType: String? = null,
        dateValue: java.util.Date? = null,
        weightFilterType: String? = null,
        weightValue: Double? = null
    ): Flow<List<Pesagem>>
    suspend fun insertPesagem(pesagem: Pesagem): Long
    suspend fun updatePesagem(pesagem: Pesagem)
    suspend fun deletePesagem(pesagem: Pesagem)
}

