package com.example.projectapki.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.projectapki.ui.components.MiniChart
import com.example.projectapki.ui.components.StatusPill
import com.example.projectapki.util.ExportUtils
import com.example.projectapki.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun startOfTodayMs(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: MainViewModel,
    padding: PaddingValues,
    goHistory: () -> Unit,
    goZones: () -> Unit,
    goDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val loc = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val mic = res[Manifest.permission.RECORD_AUDIO] == true
        val cam = res[Manifest.permission.CAMERA] == true

        vm.setPermissions(loc, mic, cam)
        vm.startSensors(context)
    }

    var pendingPhotoUri by remember { mutableStateOf(android.net.Uri.EMPTY) }
    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) vm.saveMeasurement(photoUri = pendingPhotoUri.toString())
    }

    LaunchedEffect(Unit) {
        val locGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val micGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val camGranted =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        vm.setPermissions(locGranted, micGranted, camGranted)

        if (!locGranted || !micGranted || !camGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            )
        } else {
            vm.startSensors(context)
        }
    }

    val activeZone = state.activeZone
    val noiseLimit = activeZone?.maxNoiseDb
    val accelLimit = activeZone?.maxAccel

    val noiseOk = noiseLimit?.let { state.soundDbApprox <= it } ?: true
    val accelOk = accelLimit?.let { state.accelMagnitude <= it } ?: true
    val okAll = noiseOk && accelOk

    val dayStartMs = remember { startOfTodayMs() }
    val todays = remember(state.measurements) { state.measurements.filter { it.timestampMs >= dayStartMs } }

    val todayNoise = remember(todays) { todays.map { it.soundDbApprox } }
    val todayAvg = remember(todayNoise) { if (todayNoise.isEmpty()) 0.0 else todayNoise.average() }
    val todayMin = remember(todayNoise) { todayNoise.minOrNull() ?: 0.0 }
    val todayMax = remember(todayNoise) { todayNoise.maxOrNull() ?: 0.0 }

    val zonesById = remember(state.zones) { state.zones.associateBy { it.id } }

    val todayAlerts = remember(todays, zonesById) {
        todays.count { m ->
            val z = m.zoneId?.let { zonesById[it] } ?: return@count false
            (m.soundDbApprox > z.maxNoiseDb) || (m.accelMagnitude > z.maxAccel)
        }
    }

    val loudestToday = remember(todays) { todays.maxByOrNull { it.soundDbApprox } }
    val loudestZoneName = remember(loudestToday, zonesById) {
        loudestToday?.zoneId?.let { zonesById[it]?.name } ?: "brak strefy"
    }

    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val lastNoise = state.measurements.take(20).map { it.soundDbApprox }.reversed()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scroll), // ✅ TO JEST TA NAPRAWA
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Sensor Logger", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = goZones) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Strefy")
                }
                IconButton(onClick = goHistory) {
                    Icon(Icons.Default.History, contentDescription = "Historia")
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(text = "📍 ${activeZone?.name ?: "Poza strefą"}")
            StatusPill(text = if (okAll) "✅ OK" else "🚨 ALERT")
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Live sensory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Text(
                    "GPS: ${state.lat?.let { "%.6f".format(it) } ?: "brak"} , ${state.lon?.let { "%.6f".format(it) } ?: "brak"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("🎤 ${"%.1f".format(state.soundDbApprox)} dB") },
                        leadingIcon = { Icon(Icons.Default.VolumeUp, null) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("📈 ${"%.2f".format(state.accelMagnitude)} |a|") }
                    )
                }

                if (activeZone != null) {
                    Text(
                        "Progi strefy: hałas ≤ ${activeZone.maxNoiseDb.toInt()} | ruch ≤ ${activeZone.maxAccel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Statystyki dnia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(onClick = {}, label = { Text("📌 ${todays.size} zapisów") })
                    AssistChip(onClick = {}, label = { Text("🚨 alerty: $todayAlerts") })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(onClick = {}, label = { Text("AVG ${"%.1f".format(todayAvg)}") })
                    AssistChip(onClick = {}, label = { Text("MIN ${"%.1f".format(todayMin)}") })
                    AssistChip(onClick = {}, label = { Text("MAX ${"%.1f".format(todayMax)}") })
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = loudestToday?.id != null && loudestToday.id != 0L) {
                    goDetail(loudestToday!!.id)
                }
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Najgłośniejszy pomiar dnia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                if (loudestToday == null) {
                    Text("Brak zapisów. Zrób pierwszy pomiar 👇", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AssistChip(onClick = {}, label = { Text("🕒 ${timeFmt.format(Date(loudestToday.timestampMs))}") })
                        AssistChip(onClick = {}, label = { Text("📍 $loudestZoneName") })
                    }

                    Text(
                        text = "🎤 ${"%.1f".format(loudestToday.soundDbApprox)} dB",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (!loudestToday.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = loudestToday.photoUri,
                            contentDescription = "miniatura",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    } else {
                        Text(
                            "Brak zdjęcia. Kliknij „Foto + zapis” przy kolejnym pomiarze.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Wykres hałasu (ostatnie 20)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (lastNoise.size >= 2) MiniChart(values = lastNoise)
                else Text("Zapisz kilka pomiarów, żeby zobaczyć wykres.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { vm.saveMeasurement() }
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Zapisz pomiar")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = state.hasCamPerm,
                onClick = {
                    val uri = ExportUtils.createPhotoUri(context)
                    pendingPhotoUri = uri
                    takePhoto.launch(uri)
                }
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Foto + zapis")
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val csvUri = ExportUtils.exportCsv(context, state.measurements)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Eksport pomiarów")
                    putExtra(Intent.EXTRA_STREAM, csvUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Udostępnij CSV"))
            }
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Eksport CSV")
        }

        Spacer(Modifier.height(18.dp))
    }
}
