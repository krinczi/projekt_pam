package com.example.projectapki.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun MiniChart(values: List<Double>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (values.size < 2) return@Canvas

        val maxV = values.maxOrNull() ?: 1.0
        val minV = values.minOrNull() ?: 0.0
        val range = max(1e-6, (maxV - minV))

        val stepX = size.width / (values.size - 1)

        for (i in 0 until values.size - 1) {
            val x1 = i * stepX
            val x2 = (i + 1) * stepX

            val y1 = size.height - (((values[i] - minV) / range) * size.height).toFloat()
            val y2 = size.height - (((values[i + 1] - minV) / range) * size.height).toFloat()

            drawLine(
                color = Color.White,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 5f
            )
        }
    }
}
