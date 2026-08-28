package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun InteractiveBarChart(
    entries: List<FrequencyTableEntry>,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    if (entries.isEmpty()) {
        EmptyChartPlaceholder("No hay datos para mostrar el diagrama de barras")
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxFreq = max(1, entries.maxOfOrNull { it.absoluteFrequency } ?: 1)

    val gridColor = if (isDark) Nord3.copy(alpha = 0.35f) else Nord4.copy(alpha = 0.7f)
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY
    val barBaseColor = Nord8
    val barSelectedColor = Nord13

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(entries) {
                    detectTapGestures { offset ->
                        val paddingLeft = 50f
                        val paddingRight = 30f
                        val plotWidth = size.width - paddingLeft - paddingRight
                        val barSlotWidth = plotWidth / entries.size
                        val tappedIndex = ((offset.x - paddingLeft) / barSlotWidth).toInt()
                        selectedIndex = if (tappedIndex in entries.indices) {
                            if (selectedIndex == tappedIndex) null else tappedIndex
                        } else null
                    }
                }
        ) {
            val paddingLeft = 50f
            val paddingBottom = 60f
            val paddingTop = 40f
            val paddingRight = 30f

            val width = size.width
            val height = size.height
            val plotWidth = width - paddingLeft - paddingRight
            val plotHeight = height - paddingTop - paddingBottom

            // Draw Y-Axis Grid lines & labels
            val ySteps = 4
            for (i in 0..ySteps) {
                val fraction = i.toFloat() / ySteps
                val y = paddingTop + plotHeight * (1f - fraction)
                val labelVal = (maxFreq * fraction).roundToInt()

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                drawContext.canvas.nativeCanvas.drawText(
                    labelVal.toString(),
                    paddingLeft - 12f,
                    y + 5f,
                    Paint().apply {
                        color = textColor
                        textSize = 24f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                )
            }

            // Draw X and Y Axes
            drawLine(
                color = gridColor.copy(alpha = 0.8f),
                start = Offset(paddingLeft, paddingTop),
                end = Offset(paddingLeft, height - paddingBottom),
                strokeWidth = 2f
            )
            drawLine(
                color = gridColor.copy(alpha = 0.8f),
                start = Offset(paddingLeft, height - paddingBottom),
                end = Offset(width - paddingRight, height - paddingBottom),
                strokeWidth = 2f
            )

            // Draw Bars
            val slotWidth = plotWidth / entries.size
            val barWidth = slotWidth * 0.65f

            entries.forEachIndexed { index, entry ->
                val freq = entry.absoluteFrequency
                val barHeight = (freq.toFloat() / maxFreq) * plotHeight
                val left = paddingLeft + index * slotWidth + (slotWidth - barWidth) / 2f
                val top = height - paddingBottom - barHeight
                val isSelected = selectedIndex == index

                // Bar Gradient
                val brush = if (isSelected) {
                    Brush.verticalGradient(listOf(Nord13, Nord12))
                } else {
                    val c1 = StatisticsCalculator.PALETTE_COLORS[index % StatisticsCalculator.PALETTE_COLORS.size]
                    Brush.verticalGradient(
                        listOf(
                            Color(c1),
                            Color(c1).copy(alpha = 0.7f)
                        )
                    )
                }

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Value above bar
                if (freq > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        freq.toString(),
                        left + barWidth / 2f,
                        top - 8f,
                        Paint().apply {
                            color = if (isSelected) android.graphics.Color.YELLOW else textColor
                            textSize = 26f
                            textAlign = Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                    )
                }

                // X label under bar
                val label = if (entry.label.length > 8) entry.label.take(7) + "…" else entry.label
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    left + barWidth / 2f,
                    height - paddingBottom + 30f,
                    Paint().apply {
                        color = textColor
                        textSize = 22f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }

        // Selection Tooltip Overlay
        selectedIndex?.let { idx ->
            if (idx in entries.indices) {
                val item = entries[idx]
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Nord8)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${item.label}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Nord8
                        )
                        Text(
                            text = "Frec = ${item.absoluteFrequency} (${String.format(java.util.Locale.US, "%.1f", item.percentage)}%)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveHistogramChart(
    freqTable: FrequencyTable,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val entries = freqTable.entries
    if (entries.isEmpty()) {
        EmptyChartPlaceholder("No hay intervalos suficientes para generar el histograma")
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxFreq = max(1, entries.maxOfOrNull { it.absoluteFrequency } ?: 1)
    val gridColor = if (isDark) Nord3.copy(alpha = 0.35f) else Nord4.copy(alpha = 0.7f)
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(entries) {
                    detectTapGestures { offset ->
                        val paddingLeft = 50f
                        val paddingRight = 30f
                        val plotWidth = size.width - paddingLeft - paddingRight
                        val binWidth = plotWidth / entries.size
                        val tapped = ((offset.x - paddingLeft) / binWidth).toInt()
                        selectedIndex = if (tapped in entries.indices) {
                            if (selectedIndex == tapped) null else tapped
                        } else null
                    }
                }
        ) {
            val paddingLeft = 50f
            val paddingBottom = 60f
            val paddingTop = 40f
            val paddingRight = 30f

            val width = size.width
            val height = size.height
            val plotWidth = width - paddingLeft - paddingRight
            val plotHeight = height - paddingTop - paddingBottom

            // Y Grid Lines
            val ySteps = 4
            for (i in 0..ySteps) {
                val fraction = i.toFloat() / ySteps
                val y = paddingTop + plotHeight * (1f - fraction)
                val labelVal = (maxFreq * fraction).roundToInt()

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(width - paddingRight, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                drawContext.canvas.nativeCanvas.drawText(
                    labelVal.toString(),
                    paddingLeft - 10f,
                    y + 6f,
                    Paint().apply {
                        color = textColor
                        textSize = 24f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                )
            }

            // Axes
            drawLine(
                color = gridColor.copy(alpha = 0.8f),
                start = Offset(paddingLeft, paddingTop),
                end = Offset(paddingLeft, height - paddingBottom),
                strokeWidth = 2f
            )
            drawLine(
                color = gridColor.copy(alpha = 0.8f),
                start = Offset(paddingLeft, height - paddingBottom),
                end = Offset(width - paddingRight, height - paddingBottom),
                strokeWidth = 2f
            )

            // Contiguous Bins (Gap = 0 to represent continuous distribution)
            val binWidth = plotWidth / entries.size

            entries.forEachIndexed { index, entry ->
                val freq = entry.absoluteFrequency
                val binHeight = (freq.toFloat() / maxFreq) * plotHeight
                val left = paddingLeft + index * binWidth
                val top = height - paddingBottom - binHeight
                val isSelected = selectedIndex == index

                // Bin Body
                val binBrush = if (isSelected) {
                    Brush.verticalGradient(listOf(Nord14, Nord14.copy(alpha = 0.6f)))
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Nord9.copy(alpha = 0.85f),
                            Nord10.copy(alpha = 0.65f)
                        )
                    )
                }

                drawRect(
                    brush = binBrush,
                    topLeft = Offset(left, top),
                    size = Size(binWidth, binHeight)
                )

                // Bin Border Line (Separating adjacent bins clearly)
                drawRect(
                    color = if (isSelected) Nord14 else Nord0.copy(alpha = 0.5f),
                    topLeft = Offset(left, top),
                    size = Size(binWidth, binHeight),
                    style = Stroke(width = 1.5f)
                )

                // Frequency count on top
                if (freq > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        freq.toString(),
                        left + binWidth / 2f,
                        top - 6f,
                        Paint().apply {
                            color = if (isSelected) android.graphics.Color.GREEN else textColor
                            textSize = 24f
                            textAlign = Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                    )
                }

                // X Boundary Markings
                val lowerLabel = StatisticsCalculator.formatNum(entry.lowerBound)
                drawContext.canvas.nativeCanvas.drawText(
                    lowerLabel,
                    left,
                    height - paddingBottom + 26f,
                    Paint().apply {
                        color = textColor
                        textSize = 20f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )

                // Last upper bound
                if (index == entries.lastIndex) {
                    val upperLabel = StatisticsCalculator.formatNum(entry.upperBound)
                    drawContext.canvas.nativeCanvas.drawText(
                        upperLabel,
                        left + binWidth,
                        height - paddingBottom + 26f,
                        Paint().apply {
                            color = textColor
                            textSize = 20f
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }
        }

        // Tooltip
        selectedIndex?.let { idx ->
            val item = entries[idx]
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Nord14)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Intervalo ${item.label}:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Nord14
                    )
                    Text(
                        text = "Marca xi = ${StatisticsCalculator.formatNum(item.classMark)} | fi = ${item.absoluteFrequency}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveFrequencyPolygonChart(
    freqTable: FrequencyTable,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val entries = freqTable.entries
    if (entries.isEmpty()) {
        EmptyChartPlaceholder("No hay datos para trazar el polígono de frecuencias")
        return
    }

    val maxFreq = max(1, entries.maxOfOrNull { it.absoluteFrequency } ?: 1)
    val gridColor = if (isDark) Nord3.copy(alpha = 0.35f) else Nord4.copy(alpha = 0.7f)
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    Canvas(modifier = modifier.fillMaxSize()) {
        val paddingLeft = 50f
        val paddingBottom = 60f
        val paddingTop = 40f
        val paddingRight = 30f

        val width = size.width
        val height = size.height
        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        // Y Grid Lines
        val ySteps = 4
        for (i in 0..ySteps) {
            val fraction = i.toFloat() / ySteps
            val y = paddingTop + plotHeight * (1f - fraction)
            val labelVal = (maxFreq * fraction).roundToInt()

            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )

            drawContext.canvas.nativeCanvas.drawText(
                labelVal.toString(),
                paddingLeft - 10f,
                y + 6f,
                Paint().apply {
                    color = textColor
                    textSize = 24f
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
            )
        }

        // Axes
        drawLine(
            color = gridColor.copy(alpha = 0.8f),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, height - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = gridColor.copy(alpha = 0.8f),
            start = Offset(paddingLeft, height - paddingBottom),
            end = Offset(width - paddingRight, height - paddingBottom),
            strokeWidth = 2f
        )

        // Class mark polygon points including zero grounds at boundaries
        val binWidth = plotWidth / (entries.size + 1)
        val points = mutableListOf<Offset>()

        // Left zero boundary
        points.add(Offset(paddingLeft + binWidth * 0.5f, height - paddingBottom))

        // Center class marks
        entries.forEachIndexed { index, entry ->
            val cx = paddingLeft + (index + 1) * binWidth
            val cy = height - paddingBottom - (entry.absoluteFrequency.toFloat() / maxFreq) * plotHeight
            points.add(Offset(cx, cy))
        }

        // Right zero boundary
        points.add(Offset(paddingLeft + (entries.size + 1) * binWidth, height - paddingBottom))

        // Shaded Area Under Curve
        val areaPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (p in points) {
                lineTo(p.x, p.y)
            }
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                listOf(
                    Nord8.copy(alpha = 0.45f),
                    Nord8.copy(alpha = 0.05f)
                )
            )
        )

        // Connecting Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = linePath,
            color = Nord8,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Node points with value badges
        entries.forEachIndexed { index, entry ->
            val cx = paddingLeft + (index + 1) * binWidth
            val cy = height - paddingBottom - (entry.absoluteFrequency.toFloat() / maxFreq) * plotHeight

            // Outer glow
            drawCircle(
                color = Nord8.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(cx, cy)
            )
            // Inner node
            drawCircle(
                color = Nord8,
                radius = 6f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Nord0,
                radius = 3f,
                center = Offset(cx, cy)
            )

            // Frequency text above node
            if (entry.absoluteFrequency > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    entry.absoluteFrequency.toString(),
                    cx,
                    cy - 12f,
                    Paint().apply {
                        color = textColor
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                )
            }

            // X Class Mark xi label
            val xiLabel = StatisticsCalculator.formatNum(entry.classMark)
            drawContext.canvas.nativeCanvas.drawText(
                xiLabel,
                cx,
                height - paddingBottom + 26f,
                Paint().apply {
                    color = textColor
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}

@Composable
fun InteractivePieChart(
    items: List<CategoricalItem>,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    if (items.isEmpty()) {
        EmptyChartPlaceholder("No hay categorías para graficar el diagrama circular")
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val total = items.sumOf { it.frequency }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .pointerInput(items) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val radius = size.width / 2f

                        if (dist <= radius) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            var currentAngle = 0f
                            items.forEachIndexed { index, item ->
                                val sweep = if (total > 0) ((item.frequency / total) * 360f).toFloat() else 0f
                                if (angle in currentAngle..(currentAngle + sweep)) {
                                    selectedIndex = if (selectedIndex == index) null else index
                                    return@detectTapGestures
                                }
                                currentAngle += sweep
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.width / 2f) - 20f
                var currentAngle = -90f // Start from top

                items.forEachIndexed { index, item ->
                    val sweep = if (total > 0) ((item.frequency / total) * 360f).toFloat() else 0f
                    val isSelected = selectedIndex == index
                    val sliceRadius = if (isSelected) baseRadius + 8f else baseRadius
                    val color = Color(item.colorHex)

                    // Draw Slice
                    drawArc(
                        color = color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(center.x - sliceRadius, center.y - sliceRadius),
                        size = Size(sliceRadius * 2, sliceRadius * 2)
                    )

                    // Inner border separation
                    drawArc(
                        color = if (isDark) Nord0 else Nord6,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(center.x - sliceRadius, center.y - sliceRadius),
                        size = Size(sliceRadius * 2, sliceRadius * 2),
                        style = Stroke(width = 2f)
                    )

                    currentAngle += sweep
                }

                // Center Donut Cutout
                val innerRadius = baseRadius * 0.52f
                drawCircle(
                    color = if (isDark) Nord0 else Nord6,
                    radius = innerRadius,
                    center = center
                )
                drawCircle(
                    color = if (isDark) Nord2.copy(alpha = 0.5f) else Nord4.copy(alpha = 0.5f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }

            // Center Stat Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                if (selectedIndex != null && selectedIndex!! in items.indices) {
                    val sel = items[selectedIndex!!]
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", sel.percentage)}%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(sel.colorHex)
                    )
                    Text(
                        text = sel.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "TOTAL N",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Nord9
                    )
                    Text(
                        text = StatisticsCalculator.formatNum(total),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Interactive Legend Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        val isSel = items.indexOf(item) == selectedIndex
                        Surface(
                            onClick = {
                                val idx = items.indexOf(item)
                                selectedIndex = if (selectedIndex == idx) null else idx
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(item.colorHex).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) Color(item.colorHex) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(item.colorHex))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${StatisticsCalculator.formatNum(item.frequency)} (${String.format(java.util.Locale.US, "%.1f", item.percentage)}%)",
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveScatterPlotChart(
    bivariateStats: BivariateStats,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val points = bivariateStats.points
    if (points.isEmpty()) {
        EmptyChartPlaceholder("No hay pares (X, Y) para generar el diagrama de dispersión")
        return
    }

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    val minY = points.minOf { it.y }
    val maxY = points.maxOf { it.y }

    val padX = max(1.0, (maxX - minX) * 0.15)
    val padY = max(1.0, (maxY - minY) * 0.15)

    val domainMin = minX - padX
    val domainMax = maxX + padX
    val rangeMin = minY - padY
    val rangeMax = maxY + padY

    val gridColor = if (isDark) Nord3.copy(alpha = 0.35f) else Nord4.copy(alpha = 0.7f)
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingLeft = 50f
            val paddingBottom = 60f
            val paddingTop = 40f
            val paddingRight = 30f

            val width = size.width
            val height = size.height
            val plotWidth = width - paddingLeft - paddingRight
            val plotHeight = height - paddingTop - paddingBottom

            // Coordinate conversion
            fun mapX(x: Double): Float = paddingLeft + ((x - domainMin) / (domainMax - domainMin)).toFloat() * plotWidth
            fun mapY(y: Double): Float = height - paddingBottom - ((y - rangeMin) / (rangeMax - rangeMin)).toFloat() * plotHeight

            // Grid Lines Y
            val steps = 4
            for (i in 0..steps) {
                val f = i.toFloat() / steps
                val yVal = rangeMin + f * (rangeMax - rangeMin)
                val py = mapY(yVal)

                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, py),
                    end = Offset(width - paddingRight, py),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                drawContext.canvas.nativeCanvas.drawText(
                    StatisticsCalculator.formatNum(yVal),
                    paddingLeft - 10f,
                    py + 6f,
                    Paint().apply {
                        color = textColor
                        textSize = 22f
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                )
            }

            // Grid Lines X
            for (i in 0..steps) {
                val f = i.toFloat() / steps
                val xVal = domainMin + f * (domainMax - domainMin)
                val px = mapX(xVal)

                drawLine(
                    color = gridColor,
                    start = Offset(px, paddingTop),
                    end = Offset(px, height - paddingBottom),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )

                drawContext.canvas.nativeCanvas.drawText(
                    StatisticsCalculator.formatNum(xVal),
                    px,
                    height - paddingBottom + 26f,
                    Paint().apply {
                        color = textColor
                        textSize = 20f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }

            // Axes
            drawLine(
                color = gridColor.copy(alpha = 0.8f),
                start = Offset(paddingLeft, paddingTop),
                end = Offset(paddingLeft, height - paddingBottom),
                strokeWidth = 2f
            )
            drawLine(
                color = gridColor.copy(alpha = 0.8f),
                start = Offset(paddingLeft, height - paddingBottom),
                end = Offset(width - paddingRight, height - paddingBottom),
                strokeWidth = 2f
            )

            // Linear Regression Line (y = mx + b)
            val regY1 = bivariateStats.slope * domainMin + bivariateStats.intercept
            val regY2 = bivariateStats.slope * domainMax + bivariateStats.intercept
            val p1 = Offset(mapX(domainMin), mapY(regY1))
            val p2 = Offset(mapX(domainMax), mapY(regY2))

            drawLine(
                color = Nord11,
                start = p1,
                end = p2,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Scatter Data Points
            points.forEach { pt ->
                val px = mapX(pt.x)
                val py = mapY(pt.y)

                // Outer Halo
                drawCircle(
                    color = Nord8.copy(alpha = 0.35f),
                    radius = 12f,
                    center = Offset(px, py)
                )
                // Solid Point
                drawCircle(
                    color = Nord8,
                    radius = 6f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Nord0,
                    radius = 2.5f,
                    center = Offset(px, py)
                )
            }
        }

        // Regression Badge Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 6.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Nord11)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Ajuste: ${bivariateStats.regressionEquation}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Nord11
                )
                Text(
                    text = "r = ${StatisticsCalculator.formatNum(bivariateStats.pearsonR)} | R² = ${StatisticsCalculator.formatNum(bivariateStats.rSquared)}%",
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun InteractiveBoxPlotChart(
    stats: DescriptiveStats,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    if (stats.count < 3) {
        EmptyChartPlaceholder("Se requieren al menos 3 datos para el diagrama de caja y bigotes")
        return
    }

    val minVal = stats.min
    val maxVal = stats.max
    val span = max(1.0, maxVal - minVal)
    val margin = span * 0.15
    val lowerLimit = minVal - margin
    val upperLimit = maxVal + margin

    val gridColor = if (isDark) Nord3.copy(alpha = 0.35f) else Nord4.copy(alpha = 0.7f)
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40f
            val paddingRight = 40f
            val plotWidth = width - paddingLeft - paddingRight
            val centerY = height * 0.45f
            val boxHeight = 60f

            fun mapX(v: Double): Float = paddingLeft + ((v - lowerLimit) / (upperLimit - lowerLimit)).toFloat() * plotWidth

            val xMin = mapX(stats.min)
            val xQ1 = mapX(stats.q1)
            val xMed = mapX(stats.median)
            val xQ3 = mapX(stats.q3)
            val xMax = mapX(stats.max)

            // Horizontal Whisker line (Min to Max)
            drawLine(
                color = Nord8,
                start = Offset(xMin, centerY),
                end = Offset(xMax, centerY),
                strokeWidth = 2.5f
            )

            // Whisker caps
            val capHeight = 30f
            drawLine(
                color = Nord8,
                start = Offset(xMin, centerY - capHeight / 2f),
                end = Offset(xMin, centerY + capHeight / 2f),
                strokeWidth = 3f
            )
            drawLine(
                color = Nord8,
                start = Offset(xMax, centerY - capHeight / 2f),
                end = Offset(xMax, centerY + capHeight / 2f),
                strokeWidth = 3f
            )

            // IQR Box (Q1 to Q3)
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Nord9.copy(alpha = 0.6f),
                        Nord8.copy(alpha = 0.6f)
                    )
                ),
                topLeft = Offset(xQ1, centerY - boxHeight / 2f),
                size = Size(max(4f, xQ3 - xQ1), boxHeight)
            )
            drawRect(
                color = Nord8,
                topLeft = Offset(xQ1, centerY - boxHeight / 2f),
                size = Size(max(4f, xQ3 - xQ1), boxHeight),
                style = Stroke(width = 2.5f)
            )

            // Median Line (Q2)
            drawLine(
                color = Nord13,
                start = Offset(xMed, centerY - boxHeight / 2f),
                end = Offset(xMed, centerY + boxHeight / 2f),
                strokeWidth = 4f
            )

            // Mean Mark (X)
            val xMean = mapX(stats.mean)
            val crossSize = 8f
            drawLine(
                color = Nord11,
                start = Offset(xMean - crossSize, centerY - crossSize),
                end = Offset(xMean + crossSize, centerY + crossSize),
                strokeWidth = 2f
            )
            drawLine(
                color = Nord11,
                start = Offset(xMean - crossSize, centerY + crossSize),
                end = Offset(xMean + crossSize, centerY - crossSize),
                strokeWidth = 2f
            )

            // Outliers if any
            stats.outliers.forEach { outVal ->
                val xOut = mapX(outVal)
                drawCircle(
                    color = Nord11,
                    radius = 6f,
                    center = Offset(xOut, centerY)
                )
            }

            // Labels under box
            val labels = listOf(
                "Mín: ${StatisticsCalculator.formatNum(stats.min)}" to xMin,
                "Q1: ${StatisticsCalculator.formatNum(stats.q1)}" to xQ1,
                "Me: ${StatisticsCalculator.formatNum(stats.median)}" to xMed,
                "Q3: ${StatisticsCalculator.formatNum(stats.q3)}" to xQ3,
                "Máx: ${StatisticsCalculator.formatNum(stats.max)}" to xMax
            )

            labels.forEach { (lbl, px) ->
                drawContext.canvas.nativeCanvas.drawText(
                    lbl,
                    px,
                    centerY + boxHeight / 2f + 30f,
                    Paint().apply {
                        color = textColor
                        textSize = 20f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
