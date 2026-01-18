package com.example.projectapki.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationReader {

    @SuppressLint("MissingPermission")
    fun observeLatLon(context: Context): Flow<Pair<Double?, Double?>> = flow {
        val client = LocationServices.getFusedLocationProviderClient(context)

        while (true) {
            try {
                val loc = client.lastLocation.await()
                emit(loc?.latitude to loc?.longitude)
            } catch (_: Exception) {
                emit(null to null)
            }
            delay(1500)
        }
    }

    private suspend fun Task<Location>.await(): Location? =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resume(null) }
        }
}
