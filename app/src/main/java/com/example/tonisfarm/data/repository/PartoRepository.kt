package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.Parto
import kotlinx.coroutines.flow.Flow

interface PartoRepository {
    fun getPartosByCowId(cowId: Long): Flow<List<Parto>>
    suspend fun getLastPartoByCowId(cowId: Long): Parto?
    suspend fun getPartoById(id: Long): Parto?
    fun getFilteredPartos(
        cowId: Long,
        dateFilterType: String? = null,
        dateValue: java.util.Date? = null,
        calvesCountFilter: Int? = null
    ): Flow<List<Parto>>
    suspend fun insertParto(parto: Parto): Long
    suspend fun updateParto(parto: Parto)
    suspend fun deleteParto(parto: Parto)
}

