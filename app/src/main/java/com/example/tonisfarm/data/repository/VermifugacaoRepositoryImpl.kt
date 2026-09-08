package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.VermifugacaoDao
import com.example.tonisfarm.domain.model.Vermifugacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class VermifugacaoRepositoryImpl @Inject constructor(
    private val vermifugacaoDao: VermifugacaoDao
) : VermifugacaoRepository {
    override fun getVermifugacoesByCowId(cowId: Long): Flow<List<Vermifugacao>> {
        return vermifugacaoDao.getVermifugacoesByCowId(cowId).map { entities ->
            entities.map { Vermifugacao.fromEntity(it) }
        }
    }

    override suspend fun getLastVermifugacaoByCowId(cowId: Long): Vermifugacao? {
        return vermifugacaoDao.getLastVermifugacaoByCowId(cowId)?.let { Vermifugacao.fromEntity(it) }
    }

    override suspend fun getVermifugacaoById(id: Long): Vermifugacao? {
        return vermifugacaoDao.getVermifugacaoById(id)?.let { Vermifugacao.fromEntity(it) }
    }

    override fun getFilteredVermifugacoes(
        cowId: Long,
        drugName: String?,
        dateFilterType: String?,
        dateValue: Date?
    ): Flow<List<Vermifugacao>> {
        val dateValueLong = dateValue?.time
        val dateStart = dateValue?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        val dateEnd = dateValue?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            calendar.timeInMillis
        }

        return vermifugacaoDao.getFilteredVermifugacoes(
            cowId = cowId,
            drugName = drugName,
            dateFilterType = dateFilterType,
            dateValue = dateValueLong,
            dateStart = dateStart,
            dateEnd = dateEnd
        ).map { entities ->
            entities.map { Vermifugacao.fromEntity(it) }
        }
    }

    override suspend fun insertVermifugacao(vermifugacao: Vermifugacao): Long {
        return vermifugacaoDao.insertVermifugacao(vermifugacao.toEntity())
    }

    override suspend fun updateVermifugacao(vermifugacao: Vermifugacao) {
        vermifugacaoDao.updateVermifugacao(vermifugacao.toEntity())
    }

    override suspend fun deleteVermifugacao(vermifugacao: Vermifugacao) {
        vermifugacaoDao.deleteVermifugacao(vermifugacao.toEntity())
    }
}

