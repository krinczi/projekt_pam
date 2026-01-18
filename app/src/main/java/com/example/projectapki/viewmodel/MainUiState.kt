package com.example.projectapki.viewmodel

import com.example.projectapki.data.Measurement
import com.example.projectapki.data.Zone

data class MainUiState(
    val hasLocationPerm: Boolean = false,
    val hasMicPerm: Boolean = false,
    val hasCamPerm: Boolean = false,

    val lat: Double? = null,
    val lon: Double? = null,

    val soundDbApprox: Double = 0.0,
    val accelMagnitude: Double = 0.0,

    val zones: List<Zone> = emptyList(),
    val activeZone: Zone? = null,

    val measurements: List<Measurement> = emptyList(),
    val zoneFilterId: Long? = null
)
