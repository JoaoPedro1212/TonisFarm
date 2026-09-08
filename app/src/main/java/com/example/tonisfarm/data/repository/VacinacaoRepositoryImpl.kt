package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.VacinacaoDao
import com.example.tonisfarm.domain.model.Vacinacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class VacinacaoRepositoryImpl @Inject constructor(
    private val vacinacaoDao: VacinacaoDao
) : VacinacaoRepository {
    override fun getVacinacoesByCowId(cowId: Long): Flow<List<Vacinacao>> {
        return vacinacaoDao.getVacinacoesByCowId(cowId).map { entities ->
            entities.map { Vacinacao.fromEntity(it) }
        }
    }

    override suspend fun getLastVacinacaoByCowId(cowId: Long): Vacinacao? {
        return vacinacaoDao.getLastVacinacaoByCowId(cowId)?.let { Vacinacao.fromEntity(it) }
    }

    override suspend fun getVacinacaoById(id: Long): Vacinacao? {
        return vacinacaoDao.getVacinacaoById(id)?.let { Vacinacao.fromEntity(it) }
    }

    override fun getFilteredVacinacoes(
        cowId: Long,
        vaccineName: String?,
        dateFilterType: String?,
        dateValue: Date?
    ): Flow<List<Vacinacao>> {
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

        return vacinacaoDao.getFilteredVacinacoes(
            cowId = cowId,
            vaccineName = vaccineName,
            dateFilterType = dateFilterType,
            dateValue = dateValueLong,
            dateStart = dateStart,
            dateEnd = dateEnd
        ).map { entities ->
            entities.map { Vacinacao.fromEntity(it) }
        }
    }

    override suspend fun insertVacinacao(vacinacao: Vacinacao): Long {
        return vacinacaoDao.insertVacinacao(vacinacao.toEntity())
    }

    override suspend fun updateVacinacao(vacinacao: Vacinacao) {
        vacinacaoDao.updateVacinacao(vacinacao.toEntity())
    }

    override suspend fun deleteVacinacao(vacinacao: Vacinacao) {
        vacinacaoDao.deleteVacinacao(vacinacao.toEntity())
    }
}

