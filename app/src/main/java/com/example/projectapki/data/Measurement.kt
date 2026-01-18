package com.example.projectapki.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestampMs: Long,
    val lat: Double?,
    val lon: Double?,
    val soundDbApprox: Double,
    val accelMagnitude: Double,
    val zoneId: Long? = null,
    val photoUri: String? = null
)
