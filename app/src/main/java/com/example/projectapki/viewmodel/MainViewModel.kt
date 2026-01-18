package com.example.projectapki.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectapki.data.AppDatabase
import com.example.projectapki.data.Measurement
import com.example.projectapki.data.Zone
import com.example.projectapki.repository.AppRepository
import com.example.projectapki.sensors.AccelReader
import com.example.projectapki.sensors.Geo
import com.example.projectapki.sensors.LocationReader
import com.example.projectapki.sensors.MicLevelReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(
        AppDatabase.get(app).measurementDao(),
        AppDatabase.get(app).zoneDao()
    )

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    private var micJob: Job? = null
    private var locJob: Job? = null
    private var accJob: Job? = null

    init {
        viewModelScope.launch {
            repo.observeZones().collect { zones ->
                _state.update { it.copy(zones = zones) }
                updateActiveZone()
            }
        }

        viewModelScope.launch {
            repo.observeMeasurements().collect { list ->
                _state.update { it.copy(measurements = list) }
            }
        }
    }

    fun setPermissions(loc: Boolean, mic: Boolean, cam: Boolean) {
        _state.update {
            it.copy(
                hasLocationPerm = loc,
                hasMicPerm = mic,
                hasCamPerm = cam
            )
        }
    }

    fun startSensors(context: Context) {
        if (accJob == null) {
            accJob = viewModelScope.launch {
                AccelReader.observeMagnitude(context).collect { mag ->
                    _state.update { it.copy(accelMagnitude = mag) }
                    updateActiveZone()
                }
            }
        }

        if (_state.value.hasMicPerm && micJob == null) {
            micJob = viewModelScope.launch {
                MicLevelReader.observeApproxDb(context).collect { db ->
                    _state.update { it.copy(soundDbApprox = db) }
                    updateActiveZone()
                }
            }
        }

        if (_state.value.hasLocationPerm && locJob == null) {
            locJob = viewModelScope.launch {
                LocationReader.observeLatLon(context).collect { (lat, lon) ->
                    _state.update { it.copy(lat = lat, lon = lon) }
                    updateActiveZone()
                }
            }
        }
    }

    private fun updateActiveZone() {
        val s = _state.value
        val lat = s.lat ?: return
        val lon = s.lon ?: return

        val active = s.zones.firstOrNull { z ->
            Geo.distanceMeters(lat, lon, z.lat, z.lon) <= z.radiusMeters
        }

        _state.update { it.copy(activeZone = active) }
    }

    fun addZoneFromCurrent(
        name: String,
        radiusMeters: Double,
        maxNoiseDb: Double,
        maxAccel: Double
    ) {
        val s = _state.value
        val lat = s.lat ?: return
        val lon = s.lon ?: return
        if (name.isBlank()) return

        viewModelScope.launch {
            repo.insertZone(
                Zone(
                    name = name.trim(),
                    lat = lat,
                    lon = lon,
                    radiusMeters = radiusMeters,
                    maxNoiseDb = maxNoiseDb,
                    maxAccel = maxAccel
                )
            )
        }
    }

    fun saveMeasurement(photoUri: String? = null) {
        val s = _state.value
        val zoneId = s.activeZone?.id

        viewModelScope.launch {
            repo.insertMeasurement(
                Measurement(
                    timestampMs = System.currentTimeMillis(),
                    lat = s.lat,
                    lon = s.lon,
                    soundDbApprox = s.soundDbApprox,
                    accelMagnitude = s.accelMagnitude,
                    zoneId = zoneId,
                    photoUri = photoUri
                )
            )
        }
    }

    fun clearMeasurements() {
        viewModelScope.launch { repo.clearMeasurements() }
    }

    fun clearZones() {
        viewModelScope.launch { repo.clearZones() }
    }

    fun setZoneFilter(zoneId: Long?) {
        _state.update { it.copy(zoneFilterId = zoneId) }
    }
}
