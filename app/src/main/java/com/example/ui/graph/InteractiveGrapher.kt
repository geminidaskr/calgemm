package com.example.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Ultra-smooth, high-performance interactive 2D function grapher.
 */
@Composable
fun InteractiveGrapher(
    functions: List<FunctionDefinition>,
    viewport: GraphViewport,
    onViewportChange: (GraphViewport) -> Unit,
    analysis: FunctionAnalysis?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val isDark = MaterialTheme.colorScheme.background == Nord0

    var isInspectorMode by remember { mutableStateOf(false) }
    var isInspecting by remember { mutableStateOf(false) }
    var inspectedScreenX by remember { mutableFloatStateOf(0f) }
    var showNavPad by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    // Colors
    val gridColor = if (isDark) Nord3.copy(alpha = 0.35f) else Color(0xFFD8DEE9).copy(alpha = 0.85f)
    val subGridColor = if (isDark) Nord3.copy(alpha = 0.18f) else Color(0xFFE5E9F0).copy(alpha = 0.65f)
    val axisColor = if (isDark) Nord4 else Nord2
    val labelColor = if (isDark) Nord6 else Nord1

    // Parsers memoized per expression
    val parsers = remember(functions) {
        functions.filter { it.isVisible && it.expression.isNotBlank() }.map { fn ->
            fn to MathParser(fn.expression)
        }
    }

    fun panBy(fractionX: Float, fractionY: Float) {
        val dx = viewport.width * fractionX
        val dy = viewport.height * fractionY
        onViewportChange(
            GraphViewport(
                minX = viewport.minX + dx,
                maxX = viewport.maxX + dx,
                minY = viewport.minY + dy,
                maxY = viewport.maxY + dy
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(viewport, isInspectorMode) {
                    if (isInspectorMode) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isInspecting = true
                                inspectedScreenX = offset.x
                            },
                            onDragEnd = { /* keep point visible */ },
                            onDragCancel = { isInspecting = false },
                            onDrag = { change, _ ->
                                change.consume()
                                inspectedScreenX = change.position.x
                            }
                        )
                    } else {
                        detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, _ ->
                            val currentWidth = viewport.width
                            val currentHeight = viewport.height

                            // Calculate new width & height with smooth zoom limits
                            val newWidth = (currentWidth / zoom).coerceIn(0.1f, 1000.0f)
                            val newHeight = (currentHeight / zoom).coerceIn(0.1f, 1000.0f)

                            // Math delta from touch pan
                            val dxMath = -pan.x * (currentWidth / size.width)
                            val dyMath = pan.y * (currentHeight / size.height)

                            // Zoom anchored around touch centroid
                            val centroidMathX = viewport.minX + (centroid.x / size.width) * currentWidth
                            val centroidMathY = viewport.maxY - (centroid.y / size.height) * currentHeight

                            val newCenterX = (centroidMathX - (centroid.x / size.width - 0.5f) * newWidth) + dxMath
                            val newCenterY = (centroidMathY - (0.5f - centroid.y / size.height) * newHeight) + dyMath

                            val updated = GraphViewport(
                                minX = newCenterX - newWidth / 2f,
                                maxX = newCenterX + newWidth / 2f,
                                minY = newCenterY - newHeight / 2f,
                                maxY = newCenterY + newHeight / 2f
                            )
                            onViewportChange(updated)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0 || height <= 0) return@Canvas

            // Helper coordinate transformers
            fun mathToScreenX(x: Double): Float = ((x - viewport.minX) / viewport.width * width).toFloat()
            fun mathToScreenY(y: Double): Float = ((viewport.maxY - y) / viewport.height * height).toFloat()
            fun screenToMathX(sx: Float): Double = (viewport.minX + (sx / width) * viewport.width).toDouble()

            // 1. Draw Grid & Axes
            drawCoordinateSystem(
                viewport = viewport,
                gridColor = gridColor,
                subGridColor = subGridColor,
                axisColor = axisColor,
                labelColor = labelColor,
                textMeasurer = textMeasurer
            )

            // 2. Draw Asymptotes
            analysis?.verticalAsymptotes?.forEach { va ->
                val sx = mathToScreenX(va)
                if (sx in -10f..(width + 10f)) {
                    drawLine(
                        color = Nord12.copy(alpha = 0.85f),
                        start = Offset(sx, 0f),
                        end = Offset(sx, height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }
            }

            analysis?.horizontalAsymptotes?.forEach { ha ->
                val sy = mathToScreenY(ha)
                if (sy in -10f..(height + 10f)) {
                    drawLine(
                        color = Nord12.copy(alpha = 0.85f),
                        start = Offset(0f, sy),
                        end = Offset(width, sy),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }
            }

            // 3. Draw Functions with adaptive resolution
            val stepPx = 2f // 1 evaluation every 2 pixels for crisp 60fps graph
            val numSamples = (width / stepPx).toInt().coerceAtLeast(50)

            parsers.forEach { (fn, parser) ->
                val curvePath = Path()
                var isStarted = false
                var prevY = 0f
                var prevX = 0f

                for (i in 0..numSamples) {
                    val sx = i * stepPx
                    val x = screenToMathX(sx)
                    val y = parser.evaluate(x)

                    if (y.isNaN() || y.isInfinite()) {
                        isStarted = false
                        continue
                    }

                    val sy = mathToScreenY(y)

                    // Filter out asymptote spikes
                    if (isStarted) {
                        val dy = abs(sy - prevY)
                        // If dy jump is huge and crosses opposite screen bounds, treat as asymptote break
                        if (dy > height * 1.5f && (sy < 0 && prevY > height || sy > height && prevY < 0)) {
                            isStarted = false
                        }
                    }

                    if (!isStarted) {
                        curvePath.moveTo(sx, sy)
                        isStarted = true
                    } else {
                        curvePath.lineTo(sx, sy)
                    }

                    prevX = sx
                    prevY = sy
                }

                drawPath(
                    path = curvePath,
                    color = Color(fn.colorHex),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 4. Draw Key Points (Roots, Y-Intercept, Extrema)
            analysis?.roots?.forEach { root ->
                val rx = mathToScreenX(root)
                val ry = mathToScreenY(0.0)
                if (rx in 0f..width && ry in 0f..height) {
                    drawCircle(color = Nord11, radius = 6.dp.toPx(), center = Offset(rx, ry))
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(rx, ry))
                }
            }

            analysis?.yIntercept?.let { yInt ->
                val x0 = mathToScreenX(0.0)
                val y0 = mathToScreenY(yInt)
                if (x0 in 0f..width && y0 in 0f..height) {
                    drawCircle(color = Nord14, radius = 5.dp.toPx(), center = Offset(x0, y0))
                }
            }

            analysis?.localExtrema?.forEach { pt ->
                val px = mathToScreenX(pt.x)
                val py = mathToScreenY(pt.y)
                if (px in 0f..width && py in 0f..height) {
                    val color = if (pt.pointType == PointType.LOCAL_MAX) Nord13 else Nord15
                    drawCircle(color = color, radius = 5.5.dp.toPx(), center = Offset(px, py))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(px, py))
                }
            }

            // 5. Draw Interactive Point Inspector
            if (isInspecting && parsers.isNotEmpty()) {
                val clampedSx = inspectedScreenX.coerceIn(0f, width)
                val inspectX = screenToMathX(clampedSx)
                val primaryParser = parsers.first().second
                val inspectY = primaryParser.evaluate(inspectX)

                if (!inspectY.isNaN() && !inspectY.isInfinite()) {
                    val inspectSy = mathToScreenY(inspectY)

                    // Vertical tracer line
                    drawLine(
                        color = Nord8.copy(alpha = 0.6f),
                        start = Offset(clampedSx, 0f),
                        end = Offset(clampedSx, height),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // Tracer Point
                    drawCircle(color = Nord8.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = Offset(clampedSx, inspectSy))
                    drawCircle(color = Nord8, radius = 7.dp.toPx(), center = Offset(clampedSx, inspectSy))
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(clampedSx, inspectSy))

                    // Coordinate Tooltip Label
                    val coordText = "(%.2f, %.2f)".format(inspectX, inspectY)
                    val textLayout = textMeasurer.measure(
                        text = coordText,
                        style = TextStyle(
                            color = Nord6,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    val tooltipPadding = 6.dp.toPx()
                    val tooltipW = textLayout.size.width + tooltipPadding * 2
                    val tooltipH = textLayout.size.height + tooltipPadding * 2
                    var tooltipX = clampedSx + 12.dp.toPx()
                    var tooltipY = inspectSy - tooltipH - 8.dp.toPx()

                    if (tooltipX + tooltipW > width) tooltipX = clampedSx - tooltipW - 12.dp.toPx()
                    if (tooltipY < 0) tooltipY = inspectSy + 12.dp.toPx()

                    drawRoundRect(
                        color = Nord0.copy(alpha = 0.9f),
                        topLeft = Offset(tooltipX, tooltipY),
                        size = Size(tooltipW, tooltipH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = Nord8,
                        topLeft = Offset(tooltipX, tooltipY),
                        size = Size(tooltipW, tooltipH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(1.dp.toPx())
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(tooltipX + tooltipPadding, tooltipY + tooltipPadding)
                    )
                }
            }
        }

        // Top-Start HUD: Viewport Center coordinates & mode badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = "Posición",
                        tint = Nord8,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Centro (%.1f, %.1f)".format(viewport.centerX, viewport.centerY),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isInspectorMode) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Nord8.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Nord8.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "📍 Trazador f(x)",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Nord8,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Floating Bento HUD Badges (Bottom Start)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Function expression badge
            if (functions.any { it.isVisible }) {
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    functions.filter { it.isVisible }.forEach { fn ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(fn.colorHex), CircleShape)
                            )
                            Text(
                                text = "${fn.name} = ${MathFormatter.formatToUnicode(fn.expression)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick Info Pill: Vertex or Extrema
            analysis?.localExtrema?.firstOrNull()?.let { pt ->
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "V(${pt.x}, ${pt.y})",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Nord8
                    )
                }
            }
        }

        // Floating D-Pad Navigation Controls (Bottom End if toggled)
        if (showNavPad) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { panBy(0f, 0.25f) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Arriba", tint = Nord8)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { panBy(-0.25f, 0f) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Izquierda", tint = Nord8)
                        }
                        IconButton(
                            onClick = { onViewportChange(GraphViewport(-10f, 10f, -10f, 10f)) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Outlined.CenterFocusStrong, contentDescription = "Origen", tint = Nord13)
                        }
                        IconButton(
                            onClick = { panBy(0.25f, 0f) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Derecha", tint = Nord8)
                        }
                    }
                    IconButton(
                        onClick = { panBy(0f, -0.25f) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Abajo", tint = Nord8)
                    }
                }
            }
        }

        // Floating Graph Control Buttons (Zoom in, Zoom out, Reset/Center, More Options)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.End
        ) {
            GraphActionButton(
                icon = Icons.Outlined.Add,
                contentDescription = "Acercar",
                onClick = {
                    val scale = 0.75f
                    val newW = viewport.width * scale
                    val newH = viewport.height * scale
                    onViewportChange(
                        GraphViewport(
                            minX = viewport.centerX - newW / 2f,
                            maxX = viewport.centerX + newW / 2f,
                            minY = viewport.centerY - newH / 2f,
                            maxY = viewport.centerY + newH / 2f
                        )
                    )
                }
            )
            GraphActionButton(
                icon = Icons.Outlined.Remove,
                contentDescription = "Alejar",
                onClick = {
                    val scale = 1.33f
                    val newW = viewport.width * scale
                    val newH = viewport.height * scale
                    onViewportChange(
                        GraphViewport(
                            minX = viewport.centerX - newW / 2f,
                            maxX = viewport.centerX + newW / 2f,
                            minY = viewport.centerY - newH / 2f,
                            maxY = viewport.centerY + newH / 2f
                        )
                    )
                }
            )
            GraphActionButton(
                icon = Icons.Outlined.RestartAlt,
                contentDescription = "Centrar / Origen (0,0)",
                onClick = {
                    onViewportChange(GraphViewport(-10f, 10f, -10f, 10f))
                }
            )
            Box {
                GraphActionButton(
                    icon = Icons.Outlined.MoreVert,
                    contentDescription = "Más opciones de visualización",
                    isActive = showMoreOptions || showNavPad || isInspectorMode,
                    onClick = {
                        showMoreOptions = !showMoreOptions
                    }
                )

                DropdownMenu(
                    expanded = showMoreOptions,
                    onDismissRequest = { showMoreOptions = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (showNavPad) "Ocultar Controles D-Pad" else "Mover Plano (D-Pad)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ControlCamera,
                                contentDescription = null,
                                tint = if (showNavPad) Nord8 else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showNavPad = !showNavPad
                            showMoreOptions = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isInspectorMode) "Desactivar Trazador f(x)" else "Trazador de Puntos f(x)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.TouchApp,
                                contentDescription = null,
                                tint = if (isInspectorMode) Nord8 else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            isInspectorMode = !isInspectorMode
                            if (!isInspectorMode) isInspecting = false
                            showMoreOptions = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Vista Trigonométrica (π)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "π",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Nord8
                                )
                            }
                        },
                        onClick = {
                            val twoPi = (2 * Math.PI).toFloat()
                            onViewportChange(GraphViewport(-twoPi, twoPi, -4f, 4f))
                            showMoreOptions = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Refrescar / Reajustar Plano",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = Nord8,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            onViewportChange(GraphViewport(-10f, 10f, -10f, 10f))
                            showMoreOptions = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String? = null,
    isActive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(30.dp),
        shape = RoundedCornerShape(9.dp),
        color = if (isActive) Nord8 else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) Nord8 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shadowElevation = 2.5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isActive) Nord0 else Nord8,
                    modifier = Modifier.size(15.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) Nord0 else Nord8
                )
            }
        }
    }
}

private fun DrawScope.drawCoordinateSystem(
    viewport: GraphViewport,
    gridColor: Color,
    subGridColor: Color,
    axisColor: Color,
    labelColor: Color,
    textMeasurer: TextMeasurer
) {
    val width = size.width
    val height = size.height

    fun mathToScreenX(x: Double): Float = ((x - viewport.minX) / viewport.width * width).toFloat()
    fun mathToScreenY(y: Double): Float = ((viewport.maxY - y) / viewport.height * height).toFloat()

    // Determine grid step dynamically based on zoom
    val rawStep = viewport.width / 8.0
    val magnitude = 10.0.pow(floor(log10(rawStep)))
    val normalized = rawStep / magnitude
    val step = when {
        normalized < 1.5 -> 1.0 * magnitude
        normalized < 3.5 -> 2.0 * magnitude
        normalized < 7.5 -> 5.0 * magnitude
        else -> 10.0 * magnitude
    }

    // Grid lines X
    val startX = floor(viewport.minX / step) * step
    var gx = startX
    while (gx <= viewport.maxX) {
        val sx = mathToScreenX(gx)
        drawLine(
            color = gridColor,
            start = Offset(sx, 0f),
            end = Offset(sx, height),
            strokeWidth = 1.dp.toPx()
        )
        // Numerical label
        if (abs(gx) > 1e-6) {
            val label = if (step >= 1.0) "%.0f".format(gx) else "%.1f".format(gx)
            val textLayout = textMeasurer.measure(
                text = label,
                style = TextStyle(color = labelColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            )
            val originY = mathToScreenY(0.0).coerceIn(12f, height - 20f)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(sx - textLayout.size.width / 2f, originY + 3.dp.toPx())
            )
        }
        gx += step
    }

    // Grid lines Y
    val startY = floor(viewport.minY / step) * step
    var gy = startY
    while (gy <= viewport.maxY) {
        val sy = mathToScreenY(gy)
        drawLine(
            color = gridColor,
            start = Offset(0f, sy),
            end = Offset(width, sy),
            strokeWidth = 1.dp.toPx()
        )
        // Numerical label
        if (abs(gy) > 1e-6) {
            val label = if (step >= 1.0) "%.0f".format(gy) else "%.1f".format(gy)
            val textLayout = textMeasurer.measure(
                text = label,
                style = TextStyle(color = labelColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            )
            val originX = mathToScreenX(0.0).coerceIn(4f, width - textLayout.size.width - 6f)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(originX + 4.dp.toPx(), sy - textLayout.size.height / 2f)
            )
        }
        gy += step
    }

    // Main Axes (X and Y = 0)
    val originX = mathToScreenX(0.0)
    val originY = mathToScreenY(0.0)

    if (originY in 0f..height) {
        drawLine(
            color = axisColor,
            start = Offset(0f, originY),
            end = Offset(width, originY),
            strokeWidth = 2.dp.toPx()
        )
    }

    if (originX in 0f..width) {
        drawLine(
            color = axisColor,
            start = Offset(originX, 0f),
            end = Offset(originX, height),
            strokeWidth = 2.dp.toPx()
        )
    }
}
