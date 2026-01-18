package com.example.projectapki.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class Zone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val lat: Double,
    val lon: Double,
    val radiusMeters: Double,
    val maxNoiseDb: Double,
    val maxAccel: Double
)
