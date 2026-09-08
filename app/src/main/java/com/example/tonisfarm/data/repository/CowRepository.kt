package com.example.tonisfarm.data.repository

import com.example.tonisfarm.domain.model.Cow
import kotlinx.coroutines.flow.Flow

interface CowRepository {
    fun getAllCows(): Flow<List<Cow>>
    suspend fun getCowById(id: Long): Cow?
    fun searchCows(search: String): Flow<List<Cow>>
    fun getPregnantCows(): Flow<List<Cow>>
    fun getTotalCowsCount(): Flow<Int>
    fun getPregnantCowsCount(): Flow<Int>
    suspend fun insertCow(cow: Cow): Long
    suspend fun updateCow(cow: Cow)
    suspend fun deleteCow(cow: Cow)
}

