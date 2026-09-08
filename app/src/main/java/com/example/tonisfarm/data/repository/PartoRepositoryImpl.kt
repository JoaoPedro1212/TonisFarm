package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.PartoDao
import com.example.tonisfarm.domain.model.Parto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class PartoRepositoryImpl @Inject constructor(
    private val partoDao: PartoDao
) : PartoRepository {
    override fun getPartosByCowId(cowId: Long): Flow<List<Parto>> {
        return partoDao.getPartosByCowId(cowId).map { entities ->
            entities.map { Parto.fromEntity(it) }
        }
    }

    override suspend fun getLastPartoByCowId(cowId: Long): Parto? {
        return partoDao.getLastPartoByCowId(cowId)?.let { Parto.fromEntity(it) }
    }

    override suspend fun getPartoById(id: Long): Parto? {
        return partoDao.getPartoById(id)?.let { Parto.fromEntity(it) }
    }

    override fun getFilteredPartos(
        cowId: Long,
        dateFilterType: String?,
        dateValue: Date?,
        calvesCountFilter: Int?
    ): Flow<List<Parto>> {
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

        return partoDao.getFilteredPartos(
            cowId = cowId,
            dateFilterType = dateFilterType,
            dateValue = dateValueLong,
            dateStart = dateStart,
            dateEnd = dateEnd,
            calvesCountFilter = calvesCountFilter
        ).map { entities ->
            entities.map { Parto.fromEntity(it) }
        }
    }

    override suspend fun insertParto(parto: Parto): Long {
        return partoDao.insertParto(parto.toEntity())
    }

    override suspend fun updateParto(parto: Parto) {
        partoDao.updateParto(parto.toEntity())
    }

    override suspend fun deleteParto(parto: Parto) {
        partoDao.deleteParto(parto.toEntity())
    }
}

