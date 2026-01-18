package com.example.projectapki.repository

import com.example.projectapki.data.Measurement
import com.example.projectapki.data.MeasurementDao
import com.example.projectapki.data.Zone
import com.example.projectapki.data.ZoneDao
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val mDao: MeasurementDao,
    private val zDao: ZoneDao
) {
    fun observeMeasurements(): Flow<List<Measurement>> = mDao.observeAll()
    fun observeMeasurementsByZone(zoneId: Long): Flow<List<Measurement>> = mDao.observeByZone(zoneId)

    suspend fun insertMeasurement(m: Measurement) = mDao.insert(m)
    suspend fun clearMeasurements() = mDao.deleteAll()

    fun observeZones(): Flow<List<Zone>> = zDao.observeAll()
    suspend fun insertZone(z: Zone) = zDao.insert(z)
    suspend fun clearZones() = zDao.deleteAll()
}
