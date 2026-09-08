package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.PesagemDao
import com.example.tonisfarm.domain.model.Pesagem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class PesagemRepositoryImpl @Inject constructor(
    private val pesagemDao: PesagemDao
) : PesagemRepository {
    override fun getPesagensByCowId(cowId: Long): Flow<List<Pesagem>> {
        return pesagemDao.getPesagensByCowId(cowId).map { entities ->
            entities.map { Pesagem.fromEntity(it) }
        }
    }

    override suspend fun getLastPesagemByCowId(cowId: Long): Pesagem? {
        return pesagemDao.getLastPesagemByCowId(cowId)?.let { Pesagem.fromEntity(it) }
    }

    override suspend fun getPesagemById(id: Long): Pesagem? {
        return pesagemDao.getPesagemById(id)?.let { Pesagem.fromEntity(it) }
    }

    override fun getFilteredPesagens(
        cowId: Long,
        dateFilterType: String?,
        dateValue: Date?,
        weightFilterType: String?,
        weightValue: Double?
    ): Flow<List<Pesagem>> {
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

        return pesagemDao.getFilteredPesagens(
            cowId = cowId,
            dateFilterType = dateFilterType,
            dateValue = dateValueLong,
            dateStart = dateStart,
            dateEnd = dateEnd,
            weightFilterType = weightFilterType,
            weightValue = weightValue
        ).map { entities ->
            entities.map { Pesagem.fromEntity(it) }
        }
    }

    override suspend fun insertPesagem(pesagem: Pesagem): Long {
        return pesagemDao.insertPesagem(pesagem.toEntity())
    }

    override suspend fun updatePesagem(pesagem: Pesagem) {
        pesagemDao.updatePesagem(pesagem.toEntity())
    }

    override suspend fun deletePesagem(pesagem: Pesagem) {
        pesagemDao.deletePesagem(pesagem.toEntity())
    }
}

