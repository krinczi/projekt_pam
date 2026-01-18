package com.example.projectapki.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun MetricRing(
    title: String,
    valueText: String,
    progress01: Float,
    modifier: Modifier = Modifier
) {
    val p = min(1f, max(0f, progress01))

    val ringBg: Color = MaterialTheme.colorScheme.surfaceVariant
    val ringFg: Color = MaterialTheme.colorScheme.primary
    val cardBg: Color = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 10f, cap = StrokeCap.Round)
                    val s = size

                    drawArc(
                        color = ringBg,
                        startAngle = -210f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = stroke,
                        size = Size(s.width, s.height)
                    )

                    drawArc(
                        color = ringFg,
                        startAngle = -210f,
                        sweepAngle = 240f * p,
                        useCenter = false,
                        style = stroke,
                        size = Size(s.width, s.height)
                    )
                }

                Text(
                    text = "${(p * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(valueText, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Im wyżej, tym bliżej progu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
