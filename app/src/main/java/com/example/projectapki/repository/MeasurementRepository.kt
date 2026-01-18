package com.example.projectapki.repository

import com.example.projectapki.data.Measurement
import com.example.projectapki.data.MeasurementDao
import com.example.projectapki.data.Zone
import com.example.projectapki.data.ZoneDao
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(
    private val dao: MeasurementDao,
    private val zoneDao: ZoneDao
) {
    fun observeAllMeasurements(): Flow<List<Measurement>> = dao.observeAll()
    suspend fun insertMeasurement(m: Measurement) = dao.insert(m)
    suspend fun deleteAllMeasurements() = dao.deleteAll()

    fun observeZones(): Flow<List<Zone>> = zoneDao.observeAll()
    suspend fun insertZone(z: Zone) = zoneDao.insert(z)
    suspend fun getZone(id: Long) = zoneDao.getById(id)
}
