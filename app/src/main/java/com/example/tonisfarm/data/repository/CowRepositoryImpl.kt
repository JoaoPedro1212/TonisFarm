package com.example.tonisfarm.data.repository

import com.example.tonisfarm.data.local.dao.CowDao
import com.example.tonisfarm.domain.model.Cow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CowRepositoryImpl @Inject constructor(
    private val cowDao: CowDao
) : CowRepository {
    override fun getAllCows(): Flow<List<Cow>> {
        return cowDao.getAllCows().map { entities ->
            entities.map { Cow.fromEntity(it) }
        }
    }

    override suspend fun getCowById(id: Long): Cow? {
        return cowDao.getCowById(id)?.let { Cow.fromEntity(it) }
    }

    override fun searchCows(search: String): Flow<List<Cow>> {
        return cowDao.searchCows(search).map { entities ->
            entities.map { Cow.fromEntity(it) }
        }
    }

    override fun getPregnantCows(): Flow<List<Cow>> {
        return cowDao.getPregnantCows().map { entities ->
            entities.map { Cow.fromEntity(it) }
        }
    }

    override fun getTotalCowsCount(): Flow<Int> {
        return cowDao.getTotalCowsCount()
    }

    override fun getPregnantCowsCount(): Flow<Int> {
        return cowDao.getPregnantCowsCount()
    }

    override suspend fun insertCow(cow: Cow): Long {
        return cowDao.insertCow(cow.toEntity())
    }

    override suspend fun updateCow(cow: Cow) {
        cowDao.updateCow(cow.toEntity())
    }

    override suspend fun deleteCow(cow: Cow) {
        cowDao.deleteCow(cow.toEntity())
    }
}

