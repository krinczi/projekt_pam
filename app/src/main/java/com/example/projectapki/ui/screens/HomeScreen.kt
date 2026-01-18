package com.example.projectapki.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.projectapki.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    goHistory: () -> Unit,
    goZones: () -> Unit
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val loc = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val mic = res[Manifest.permission.RECORD_AUDIO] == true
        val cam = res[Manifest.permission.CAMERA] == true

        vm.setPermissions(loc, mic, cam)
        vm.startSensors(context)
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
            launcher.launch(
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

    val df = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Logger") },
                actions = {
                    TextButton(onClick = goZones) { Text("Strefy") }
                    TextButton(onClick = goHistory) { Text("Historia") }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Live dane", style = MaterialTheme.typography.titleMedium)

                    Text(
                        "GPS: ${state.lat?.let { "%.6f".format(it) } ?: "brak"} , " +
                                "${state.lon?.let { "%.6f".format(it) } ?: "brak"}"
                    )

                    Text("Mikrofon (db-ish): ${"%.1f".format(state.soundDbApprox)}")
                    Text("Akcelerometr (|a|): ${"%.2f".format(state.accelMagnitude)} m/s²")
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { vm.saveMeasurement() }
                ) {
                    Text("Zapisz pomiar")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = goHistory
                ) {
                    Text("Zobacz historię")
                }
            }

            Divider()

            val last3 = state.measurements.take(3)

            Text(
                "Ostatnie pomiary: ${last3.size}",
                style = MaterialTheme.typography.titleSmall
            )

            last3.forEach { m ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("🕒 ${df.format(Date(m.timestampMs))}")
                        Text(
                            "GPS: ${m.lat?.let { "%.6f".format(it) } ?: "brak"} , " +
                                    "${m.lon?.let { "%.6f".format(it) } ?: "brak"}"
                        )
                        Text("Mic: ${"%.1f".format(m.soundDbApprox)}   |   Acc: ${"%.2f".format(m.accelMagnitude)}")
                    }
                }
            }

            if (state.measurements.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Brak zapisów. Kliknij „Zapisz pomiar”.")
                }
            }
        }
    }
}
