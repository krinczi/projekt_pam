package com.example.projectapki.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.projectapki.data.Measurement
import java.io.File

object ExportUtils {

    fun exportCsv(context: Context, rows: List<Measurement>): Uri {
        val file = File(context.cacheDir, "sensor_export.csv")
        file.writeText("timestamp_ms,lat,lon,noise_db,accel,zone_id,photo_uri\n")

        rows.forEach { m ->
            file.appendText(
                "${m.timestampMs},${m.lat ?: ""},${m.lon ?: ""},${m.soundDbApprox},${m.accelMagnitude},${m.zoneId ?: ""},${m.photoUri ?: ""}\n"
            )
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun createPhotoUri(context: Context): Uri {
        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
