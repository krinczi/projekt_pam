package com.example.projectapki.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert
    suspend fun insert(m: Measurement)

    @Query("SELECT * FROM measurements ORDER BY timestampMs DESC")
    fun observeAll(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE zoneId = :zoneId ORDER BY timestampMs DESC")
    fun observeByZone(zoneId: Long): Flow<List<Measurement>>

    @Query("DELETE FROM measurements")
    suspend fun deleteAll()
}
