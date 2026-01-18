package com.example.projectapki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.projectapki.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    vm: MainViewModel,
    padding: PaddingValues,
    measurementId: Long,
    goBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val df = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val m = state.measurements.firstOrNull { it.id == measurementId }
    val zone = m?.zoneId?.let { id -> state.zones.firstOrNull { it.id == id } }

    val alertNoise = zone?.let { z -> (m?.soundDbApprox ?: 0.0) > z.maxNoiseDb } ?: false
    val alertAccel = zone?.let { z -> (m?.accelMagnitude ?: 0.0) > z.maxAccel } ?: false
    val isAlert = alertNoise || alertAccel

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły pomiaru") },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (m == null) {
                Text("Nie znaleziono pomiaru :(", style = MaterialTheme.typography.titleMedium)
                return@Column
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AssistChip(onClick = {}, label = { Text(zone?.name ?: "brak strefy") })
                        if (isAlert) {
                            AssistChip(onClick = {}, label = { Text("🚨 ALERT") })
                            Icon(Icons.Default.Warning, contentDescription = null)
                        } else {
                            AssistChip(onClick = {}, label = { Text("✅ OK") })
                        }
                    }

                    Text("🕒 ${df.format(Date(m.timestampMs))}", fontWeight = FontWeight.SemiBold)

                    Text(
                        "GPS: ${m.lat?.let { "%.6f".format(it) } ?: "brak"} , " +
                                "${m.lon?.let { "%.6f".format(it) } ?: "brak"}"
                    )

                    Text("🎤 Hałas: ${"%.1f".format(m.soundDbApprox)} dB ${if (alertNoise) "🚨" else ""}")
                    Text("📈 Ruch: ${"%.2f".format(m.accelMagnitude)} |a| ${if (alertAccel) "🚨" else ""}")

                    if (!m.photoUri.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        AsyncImage(
                            model = m.photoUri,
                            contentDescription = "photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }
        }
    }
}
