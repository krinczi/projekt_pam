package com.example.projectapki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment   // ✅ TO BYŁO BRAKUJĄCE
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectapki.viewmodel.MainViewModel
import java.util.Calendar
import kotlin.math.roundToInt

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
fun ZonesScreen(vm: MainViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("80") }
    var maxNoise by remember { mutableStateOf("55") }
    var maxAccel by remember { mutableStateOf("12") }

    val dayStartMs = remember { startOfTodayMs() }
    val todays = remember(state.measurements) { state.measurements.filter { it.timestampMs >= dayStartMs } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Strefy") },
                actions = {
                    IconButton(onClick = { vm.clearZones() }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Usuń strefy")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Dodaj strefę",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "GPS: ${state.lat?.let { "%.6f".format(it) } ?: "brak"} , ${state.lon?.let { "%.6f".format(it) } ?: "brak"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nazwa strefy (np. Dom)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = radius,
                            onValueChange = { radius = it.filter(Char::isDigit) },
                            label = { Text("Promień (m)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxNoise,
                            onValueChange = { maxNoise = it.filter(Char::isDigit) },
                            label = { Text("Max hałas (dB)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = maxAccel,
                        onValueChange = { maxAccel = it.filter(Char::isDigit) },
                        label = { Text("Max ruch (|a|)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.lat != null && state.lon != null && name.isNotBlank(),
                        onClick = {
                            vm.addZoneFromCurrent(
                                name = name,
                                radiusMeters = radius.toDoubleOrNull() ?: 80.0,
                                maxNoiseDb = maxNoise.toDoubleOrNull() ?: 55.0,
                                maxAccel = maxAccel.toDoubleOrNull() ?: 12.0
                            )
                            name = ""
                        }
                    ) {
                        Text("Dodaj strefę")
                    }
                }
            }

            Text(
                "Twoje strefy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.zones) { z ->
                    val active = state.activeZone?.id == z.id

                    val zoneToday = todays.filter { it.zoneId == z.id }
                    val alertsToday = zoneToday.count { m ->
                        (m.soundDbApprox > z.maxNoiseDb) || (m.accelMagnitude > z.maxAccel)
                    }

                    Card(
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                (if (active) "🟢 " else "⚪ ") + z.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text("Promień: ${z.radiusMeters.roundToInt()} m")
                            Text("Progi: hałas ≤ ${z.maxNoiseDb.roundToInt()} | ruch ≤ ${z.maxAccel}")

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AssistChip(onClick = {}, label = { Text("📌 dziś: ${zoneToday.size}") })
                                AssistChip(onClick = {}, label = { Text("🚨 alerty: $alertsToday") })
                                if (alertsToday > 0) {
                                    Icon(Icons.Default.Warning, contentDescription = null)
                                }
                            }

                            if (active) {
                                Divider()
                                Text("Live teraz:", fontWeight = FontWeight.SemiBold)
                                Text("🎤 Hałas: ${"%.1f".format(state.soundDbApprox)} dB")
                                Text("📈 Ruch: ${"%.2f".format(state.accelMagnitude)} |a|")
                            }
                        }
                    }
                }

                if (state.zones.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Brak stref",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Dodaj np. Dom, Uczelnia, Praca i ustaw progi komfortu.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
