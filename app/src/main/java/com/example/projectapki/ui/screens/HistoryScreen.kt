package com.example.projectapki.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.projectapki.util.ExportUtils
import com.example.projectapki.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vm: MainViewModel,
    padding: PaddingValues,
    goDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()
    val df = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val zones = state.zones
    val zonesById = remember(zones) { zones.associateBy { it.id } }

    var onlyAlerts by rememberSaveable { mutableStateOf(false) }

    var menuOpen by remember { mutableStateOf(false) }
    var selectedZoneId by rememberSaveable { mutableStateOf(-1L) } // -1 = wszystkie

    val selectedZoneName = remember(zones, selectedZoneId) {
        if (selectedZoneId == -1L) "Wszystkie strefy"
        else zones.firstOrNull { it.id == selectedZoneId }?.name ?: "Wszystkie strefy"
    }

    val filtered = remember(state.measurements, onlyAlerts, selectedZoneId, zonesById) {
        state.measurements
            .asSequence()
            .filter { m ->
                if (selectedZoneId == -1L) true else m.zoneId == selectedZoneId
            }
            .filter { m ->
                if (!onlyAlerts) true
                else {
                    val z = m.zoneId?.let { zonesById[it] } ?: return@filter false
                    (m.soundDbApprox > z.maxNoiseDb) || (m.accelMagnitude > z.maxAccel)
                }
            }
            .toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historia") },
                actions = {
                    IconButton(onClick = {
                        val csvUri = ExportUtils.exportCsv(context, filtered)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "Eksport pomiarów")
                            putExtra(Intent.EXTRA_STREAM, csvUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Udostępnij CSV"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Eksport CSV")
                    }

                    IconButton(onClick = { vm.clearMeasurements() }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Usuń wszystko")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Filtry", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Wybierz strefę lub pokaż tylko alerty",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = menuOpen,
                        onExpandedChange = { menuOpen = !menuOpen }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            value = selectedZoneName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Strefa") }
                        )

                        ExposedDropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Wszystkie strefy") },
                                onClick = {
                                    selectedZoneId = -1L
                                    menuOpen = false
                                }
                            )
                            zones.forEach { z ->
                                DropdownMenuItem(
                                    text = { Text(z.name) },
                                    onClick = {
                                        selectedZoneId = z.id
                                        menuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tylko alerty 🚨", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(
                            checked = onlyAlerts,
                            onCheckedChange = { onlyAlerts = it }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(onClick = {}, label = { Text("📌 rekordy: ${filtered.size}") })
                if (selectedZoneId != -1L) AssistChip(onClick = {}, label = { Text("📍 $selectedZoneName") })
                if (onlyAlerts) AssistChip(onClick = {}, label = { Text("🚨 alerty") })
            }

            if (filtered.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Pusto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (onlyAlerts) "Brak alertów. Twoje życie jest spokojne (póki co)."
                            else "Brak zapisów. Wejdź na Dashboard i kliknij „Zapisz pomiar”.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { m ->
                        val zone = m.zoneId?.let { zonesById[it] }
                        val zoneName = zone?.name ?: "brak strefy"

                        val alertNoise = zone?.let { m.soundDbApprox > it.maxNoiseDb } ?: false
                        val alertAccel = zone?.let { m.accelMagnitude > it.maxAccel } ?: false
                        val isAlert = alertNoise || alertAccel

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = m.id != 0L) { goDetail(m.id) }
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AssistChip(onClick = {}, label = { Text("📍 $zoneName") })

                                    if (isAlert) {
                                        AssistChip(onClick = {}, label = { Text("🚨 ALERT") })
                                        Icon(Icons.Default.Warning, contentDescription = null)
                                    } else {
                                        AssistChip(onClick = {}, label = { Text("✅ OK") })
                                    }
                                }

                                Text("🕒 ${df.format(Date(m.timestampMs))}")
                                Text(
                                    "GPS: ${m.lat?.let { "%.6f".format(it) } ?: "brak"} , " +
                                            "${m.lon?.let { "%.6f".format(it) } ?: "brak"}"
                                )
                                Text("🎤 Hałas: ${"%.1f".format(m.soundDbApprox)} dB ${if (alertNoise) "🚨" else ""}")
                                Text("📈 Ruch: ${"%.2f".format(m.accelMagnitude)} |a| ${if (alertAccel) "🚨" else ""}")

                                if (!m.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = m.photoUri,
                                        contentDescription = "photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
