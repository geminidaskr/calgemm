package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PrecalcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: PrecalcViewModel,
    onNavigateToTutor: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statInput by viewModel.statInput.collectAsState()
    val statResult by viewModel.statResult.collectAsState()
    val selectedChartType by viewModel.selectedChartType.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showFormulaDetails by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        // --- Bento Header Card ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("statistics_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Nord8.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Analytics,
                                    contentDescription = "Estadística",
                                    tint = Nord8,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Calculadora Estadística",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ANÁLISIS DESCRIPTIVO & GRAFICADOR INTELIGENTE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    color = Nord9
                                )
                            }
                        }

                        // Presets Dropdown/Toggle
                        IconButton(
                            onClick = {
                                viewModel.setStatInput("14, 18, 12, 19, 15, 17, 13, 20, 16, 18, 14, 15, 17, 19, 11, 16, 18, 13, 15, 20")
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Reiniciar",
                                tint = Nord8,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Input Field with Clean Actions
                    OutlinedTextField(
                        value = statInput,
                        onValueChange = { viewModel.setStatInput(it) },
                        label = { Text("Ingresa Datos (Muestra, Categorías o Pares X, Y)", fontSize = 11.5.sp) },
                        placeholder = { Text("Ej: 12, 15, 18, 20  o  A: 30, B: 45  o  (1, 2.5), (2, 4.0)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stat_data_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = Nord8,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        trailingIcon = {
                            if (statInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setStatInput("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = "Borrar datos",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.5.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        maxLines = 2
                    )

                    // Quick Sample Presets Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "MUESTRAS RÁPIDAS:",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presets = listOf(
                                "varianza_ejemplo" to "⚡ Varianza Paso a Paso (5, 7, 1, 2, 3)",
                                "calificaciones" to "📚 Notas Examen (Histograma)",
                                "encuesta" to "📊 Encuesta (Circular)",
                                "horas_vs_nota" to "✨ Horas vs Nota (Dispersión)",
                                "edades" to "👥 Edades (Barras)",
                                "ventas" to "💼 Ventas (Barras)",
                                "pesos" to "⚖️ Pesos (Polígono/Caja)"
                            )
                            items(presets) { (key, label) ->
                                Surface(
                                    onClick = { viewModel.loadStatPreset(key) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("preset_$key")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Streamlined Sub-Tab Navigation Bar ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    0 to "📊 Gráficos",
                    1 to "⚡ Varianza",
                    2 to "📋 Frecuencias",
                    3 to "📈 Regresión"
                )

                tabs.forEach { (idx, title) ->
                    val isSelected = selectedTab == idx
                    Surface(
                        onClick = { selectedTab = idx },
                        shape = RoundedCornerShape(11.dp),
                        color = if (isSelected) Nord8 else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Nord0 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ================= TAB 0: GRÁFICOS & RESUMEN =================
        if (selectedTab == 0) {
            // Automatic AI Recommendation Banner
            statResult?.recommendation?.let { rec ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Nord8.copy(alpha = 0.09f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Nord8.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chart_recommendation_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = "Recomendación IA",
                                        tint = Nord13,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = rec.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Nord14.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "${(rec.confidence * 100).toInt()}% afinidad",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Nord14,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = rec.reason,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Chart Type Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val chartOptions = listOf(
                        Triple(RecommendedChartType.BAR_CHART, "Barras", Icons.Outlined.BarChart),
                        Triple(RecommendedChartType.HISTOGRAM, "Histograma", Icons.Outlined.ViewColumn),
                        Triple(RecommendedChartType.FREQUENCY_POLYGON, "Polígono", Icons.Outlined.ShowChart),
                        Triple(RecommendedChartType.PIE_CHART, "Circular", Icons.Outlined.PieChart),
                        Triple(RecommendedChartType.SCATTER_PLOT, "Dispersión", Icons.Outlined.BubbleChart),
                        Triple(RecommendedChartType.BOX_PLOT, "Caja", Icons.Outlined.Inbox)
                    )

                    val recommendedType = statResult?.recommendation?.recommendedType

                    items(chartOptions) { (type, label, icon) ->
                        val isSelected = selectedChartType == type
                        val isRec = recommendedType == type

                        Surface(
                            onClick = { viewModel.selectChartType(type) },
                            shape = RoundedCornerShape(11.dp),
                            color = if (isSelected) Nord8 else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Nord8 else if (isRec) Nord13.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                            ),
                            modifier = Modifier.testTag("chart_tab_${type.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Nord0 else if (isRec) Nord13 else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Nord0 else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Chart Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .testTag("interactive_statistics_chart")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        val result = statResult
                        val activeType = selectedChartType ?: result?.recommendation?.recommendedType ?: RecommendedChartType.BAR_CHART

                        if (result == null || (result.rawNumbers.isEmpty() && result.categoricalItems.isEmpty() && result.bivariatePoints.isEmpty())) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Ingresa datos en la parte superior para visualizar gráficos",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            when (activeType) {
                                RecommendedChartType.BAR_CHART -> {
                                    val entries = result.frequencyTable?.entries
                                        ?: result.categoricalItems.mapIndexed { idx, itm ->
                                            FrequencyTableEntry(
                                                index = idx + 1,
                                                label = itm.label,
                                                lowerBound = idx.toDouble(),
                                                upperBound = (idx + 1).toDouble(),
                                                classMark = idx + 0.5,
                                                absoluteFrequency = itm.frequency.toInt(),
                                                cumulativeAbsolute = 0,
                                                relativeFrequency = itm.percentage / 100.0,
                                                cumulativeRelative = 0.0,
                                                percentage = itm.percentage,
                                                pieAngleDegrees = (itm.percentage / 100.0) * 360.0
                                            )
                                        }
                                    InteractiveBarChart(
                                        entries = entries,
                                        isDark = isDarkTheme,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                RecommendedChartType.HISTOGRAM -> {
                                    if (result.frequencyTable != null) {
                                        InteractiveHistogramChart(
                                            freqTable = result.frequencyTable,
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        InteractiveBarChart(
                                            entries = emptyList(),
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                RecommendedChartType.FREQUENCY_POLYGON -> {
                                    if (result.frequencyTable != null) {
                                        InteractiveFrequencyPolygonChart(
                                            freqTable = result.frequencyTable,
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        InteractiveBarChart(
                                            entries = emptyList(),
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                RecommendedChartType.PIE_CHART -> {
                                    val items = if (result.categoricalItems.isNotEmpty()) {
                                        result.categoricalItems
                                    } else {
                                        result.frequencyTable?.entries?.mapIndexed { idx, entry ->
                                            CategoricalItem(
                                                label = entry.label,
                                                frequency = entry.absoluteFrequency.toDouble(),
                                                percentage = entry.percentage,
                                                colorHex = StatisticsCalculator.PALETTE_COLORS[idx % StatisticsCalculator.PALETTE_COLORS.size]
                                            )
                                        } ?: emptyList()
                                    }
                                    InteractivePieChart(
                                        items = items,
                                        isDark = isDarkTheme,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                RecommendedChartType.SCATTER_PLOT -> {
                                    if (result.bivariateStats != null) {
                                        InteractiveScatterPlotChart(
                                            bivariateStats = result.bivariateStats,
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        val pts = result.rawNumbers.mapIndexed { idx, num ->
                                            BivariatePoint((idx + 1).toDouble(), num)
                                        }
                                        val bStats = StatisticsCalculator.calculateBivariateStats(pts)
                                        InteractiveScatterPlotChart(
                                            bivariateStats = bStats,
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                RecommendedChartType.BOX_PLOT -> {
                                    if (result.descriptiveStats != null) {
                                        InteractiveBoxPlotChart(
                                            stats = result.descriptiveStats,
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        InteractiveBarChart(
                                            entries = emptyList(),
                                            isDark = isDarkTheme,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Descriptive Statistics Bento Grid
            statResult?.descriptiveStats?.let { stats ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MEDIDAS DESCRIPTIVAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(
                                onClick = { showFormulaDetails = !showFormulaDetails },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = if (showFormulaDetails) "Ocultar Fórmulas" else "Ver Fórmulas",
                                    fontSize = 10.5.sp,
                                    color = Nord8
                                )
                            }
                        }

                        // 4-Card Bento Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBentoCard(
                                title = "Tendencia Central",
                                icon = Icons.Outlined.CenterFocusStrong,
                                accentColor = Nord8,
                                modifier = Modifier.weight(1f),
                                items = listOf(
                                    StatMetricItem("Media (x̄)", StatisticsCalculator.formatNum(stats.mean), isPrimary = true),
                                    StatMetricItem("Mediana (Me)", StatisticsCalculator.formatNum(stats.median)),
                                    StatMetricItem("Moda (Mo)", if (stats.modes.isNotEmpty()) stats.modes.joinToString(", ") { StatisticsCalculator.formatNum(it) } else "Amodal"),
                                    StatMetricItem("Muestra (n)", stats.count.toString())
                                )
                            )

                            StatBentoCard(
                                title = "Dispersión",
                                icon = Icons.Outlined.LinearScale,
                                accentColor = Nord14,
                                modifier = Modifier.weight(1f),
                                items = listOf(
                                    StatMetricItem("Desv. Est. (s)", StatisticsCalculator.formatNum(stats.sampleStdDev), isPrimary = true),
                                    StatMetricItem("Varianza (s²)", StatisticsCalculator.formatNum(stats.sampleVariance)),
                                    StatMetricItem("Coef. Var. (CV)", "${StatisticsCalculator.formatNum(stats.coefficientOfVariation)}%"),
                                    StatMetricItem("Rango (R)", StatisticsCalculator.formatNum(stats.range))
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBentoCard(
                                title = "Cuartiles & Rango",
                                icon = Icons.Outlined.ViewWeek,
                                accentColor = Nord13,
                                modifier = Modifier.weight(1f),
                                items = listOf(
                                    StatMetricItem("Primer Cuartil (Q1)", StatisticsCalculator.formatNum(stats.q1)),
                                    StatMetricItem("Segundo Cuartil (Q2)", StatisticsCalculator.formatNum(stats.q2)),
                                    StatMetricItem("Tercer Cuartil (Q3)", StatisticsCalculator.formatNum(stats.q3)),
                                    StatMetricItem("Rango IQR", StatisticsCalculator.formatNum(stats.iqr))
                                )
                            )

                            StatBentoCard(
                                title = "Límites & Atípicos",
                                icon = Icons.Outlined.Adjust,
                                accentColor = Nord11,
                                modifier = Modifier.weight(1f),
                                items = listOf(
                                    StatMetricItem("Mínimo", StatisticsCalculator.formatNum(stats.min)),
                                    StatMetricItem("Máximo", StatisticsCalculator.formatNum(stats.max)),
                                    StatMetricItem("Atípicos", if (stats.outliers.isNotEmpty()) stats.outliers.joinToString(", ") { StatisticsCalculator.formatNum(it) } else "Ninguno"),
                                    StatMetricItem("Asimetría", StatisticsCalculator.formatNum(stats.skewness))
                                )
                            )
                        }
                    }
                }
            }
        }

        // ================= TAB 1: VARIANZA PASO A PASO =================
        if (selectedTab == 1) {
            statResult?.varianceProcedure?.let { proc ->
                item {
                    VarianceProcedureCard(
                        procedure = proc,
                        isDark = isDarkTheme
                    )
                }
            } ?: item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingresa datos numéricos para ver el procedimiento detallado de varianza",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ================= TAB 2: TABLA DE FRECUENCIAS =================
        if (selectedTab == 2) {
            statResult?.frequencyTable?.let { table ->
                if (table.entries.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.TableChart,
                                            contentDescription = "Tabla",
                                            tint = Nord8,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Distribución de Frecuencias",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = "k = ${table.numClasses} clases | n = ${table.totalCount}",
                                        fontSize = 10.sp,
                                        color = Nord9,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Table Content with horizontal scroll
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    color = Nord8.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            TableHeaderCell("Clase / Intervalo", width = 105.dp)
                                            TableHeaderCell("xi (Marca)", width = 70.dp)
                                            TableHeaderCell("fi (Abs)", width = 55.dp)
                                            TableHeaderCell("Fi (Acum)", width = 60.dp)
                                            TableHeaderCell("hi (Rel)", width = 60.dp)
                                            TableHeaderCell("Hi (Rel Acum)", width = 80.dp)
                                            TableHeaderCell("hi %", width = 55.dp)
                                            TableHeaderCell("θ° (Sector)", width = 65.dp)
                                        }

                                        table.entries.forEachIndexed { index, entry ->
                                            Row(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else Color.Transparent,
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TableCell(entry.label, width = 105.dp, isBold = true)
                                                TableCell(StatisticsCalculator.formatNum(entry.classMark), width = 70.dp)
                                                TableCell(entry.absoluteFrequency.toString(), width = 55.dp, color = Nord8)
                                                TableCell(entry.cumulativeAbsolute.toString(), width = 60.dp)
                                                TableCell(String.format(java.util.Locale.US, "%.3f", entry.relativeFrequency), width = 60.dp)
                                                TableCell(String.format(java.util.Locale.US, "%.3f", entry.cumulativeRelative), width = 80.dp)
                                                TableCell("${String.format(java.util.Locale.US, "%.1f", entry.percentage)}%", width = 55.dp, color = Nord14)
                                                TableCell("${String.format(java.util.Locale.US, "%.1f", entry.pieAngleDegrees)}°", width = 65.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingresa datos numéricos para calcular la tabla de frecuencias",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ================= TAB 3: REGRESIÓN & DISPERSIÓN =================
        if (selectedTab == 3) {
            statResult?.bivariateStats?.let { bStats ->
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Nord11.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TrendingUp,
                                    contentDescription = "Regresión",
                                    tint = Nord11,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Regresión Lineal & Pearson",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Equation Display
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Nord11.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "RECTA DE AJUSTE (MÍNIMOS CUADRADOS)",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Nord11
                                    )
                                    Text(
                                        text = bStats.regressionEquation,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Correlation Metrics Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MetricChip(
                                    label = "Pearson r",
                                    value = StatisticsCalculator.formatNum(bStats.pearsonR),
                                    color = Nord8,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricChip(
                                    label = "R² Ajuste",
                                    value = "${StatisticsCalculator.formatNum(bStats.rSquared)}%",
                                    color = Nord14,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricChip(
                                    label = "Covarianza",
                                    value = StatisticsCalculator.formatNum(bStats.covariance),
                                    color = Nord13,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Text(
                                text = "📌 Diagnóstico: ${bStats.correlationType}. ${bStats.correlationInterpretation}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            } ?: item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ingresa pares ordenados como (x1, y1), (x2, y2) para calcular regresión lineal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- AI Statistical Explainer CTA ---
        item {
            Surface(
                onClick = {
                    val prompt = "Explícame el análisis estadístico descriptivo y las conclusiones de estos datos: $statInput"
                    onNavigateToTutor(prompt)
                },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Nord8.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ask_ai_statistics_button")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Nord8.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Tutor",
                            tint = Nord8,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Dudas con la interpretación?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pregunta al Tutor IA sobre este conjunto de datos",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Ir",
                        tint = Nord8,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class StatMetricItem(
    val label: String,
    val value: String,
    val isPrimary: Boolean = false
)

@Composable
private fun StatBentoCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    items: List<StatMetricItem>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                items.forEach { item ->
                    if (item.isPrimary) {
                        // Highlighted primary metric (Media / Desviación Estándar) with distinct typography & hero badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Métrica Principal",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor
                                    )
                                }
                                Text(
                                    text = item.value,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = accentColor
                                )
                            }
                        }
                    } else {
                        // Secondary metric (softer and smaller typography)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 2.5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Text(
                                text = item.value,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Nord8
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun VarianceProcedureCard(
    procedure: VarianceProcedure,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Nord8.copy(alpha = 0.4f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Title & Mean highlight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Nord8.copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Functions,
                                contentDescription = null,
                                tint = Nord8,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Procedimiento de Varianza",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Poblacional (σ²) y Muestral (s²) paso a paso",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mean indicator (x̄ = μ = value)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Nord8.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Nord8.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "x̄ = μ =",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Nord8
                        )
                        Text(
                            text = procedure.meanFormatted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Nord8
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Step 1: Step-by-Step Table matching the math board
            Text(
                text = "1. TABLA DE DESVIACIONES Y CUADRADOS:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 3-Column Deviation Table
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) Nord0.copy(alpha = 0.7f) else Nord6.copy(alpha = 0.5f)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "x",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Nord11 // Red/coral header like in photo
                            )
                        }
                        Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "xi - x̄",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Nord11
                            )
                        }
                        Box(modifier = Modifier.width(170.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "(xi - x̄)²",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    // Data rows
                    procedure.items.forEachIndexed { index, item ->
                        val rowBg = if (index % 2 == 1) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        } else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: x value
                            Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.xStr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Nord11
                                )
                            }
                            // Column 2: xi - mean breakdown (e.g. 5 - 3.6 = +1.4)
                            Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.deviationCalcStr,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            // Column 3: squared deviation (e.g. (1.4)² = 1.96)
                            Box(modifier = Modifier.width(170.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.squaredCalcStr,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Nord8.copy(alpha = 0.4f),
                        thickness = 2.dp
                    )

                    // Total / Summation row (Σ = 0 and Σ = 23.2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Σ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Nord8
                            )
                        }
                        // Sum of deviations = 0
                        Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = procedure.sumDeviationsFormatted,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Nord11
                                )
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(2.dp)
                                        .background(Nord11)
                                )
                            }
                        }
                        // Sum of squared deviations = 23.2
                        Box(modifier = Modifier.width(170.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = procedure.sumSquaredDeviationsFormatted,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Nord11
                                )
                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(2.dp)
                                        .background(Nord11)
                                )
                            }
                        }
                    }
                }
            }

            // Step 2: Formulas and Calculations for Población vs Muestra
            Text(
                text = "2. APLICACIÓN DE FÓRMULAS (COMPARATIVA):",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Población Card
                VarianceFormulaPanel(
                    typeTitle = "POBLACIÓN",
                    symbol = "σ²",
                    stdSymbol = "σ",
                    divisorLabel = "Dividir entre N (Total = ${procedure.count})",
                    accentColor = Nord8,
                    formula = "σ² = Σ(x - μ)² / N",
                    substitution = "σ² = ${procedure.sumSquaredDeviationsFormatted} / ${procedure.count}",
                    varianceResult = procedure.populationVarianceFormatted,
                    stdDevFormula = "σ = √${procedure.populationVarianceFormatted}",
                    stdDevResult = procedure.populationStdDevFormatted,
                    modifier = Modifier.weight(1f)
                )

                // Muestra Card
                VarianceFormulaPanel(
                    typeTitle = "MUESTRA",
                    symbol = "s²",
                    stdSymbol = "s",
                    divisorLabel = "Dividir entre n - 1 (${procedure.count} - 1 = ${if (procedure.count > 1) procedure.count - 1 else 1})",
                    accentColor = Nord12,
                    formula = "s² = Σ(x - x̄)² / (n - 1)",
                    substitution = "s² = ${procedure.sumSquaredDeviationsFormatted} / (${procedure.count} - 1)",
                    varianceResult = procedure.sampleVarianceFormatted,
                    stdDevFormula = "s = √${procedure.sampleVarianceFormatted}",
                    stdDevResult = procedure.sampleStdDevFormatted,
                    modifier = Modifier.weight(1f)
                )
            }

            // Educational explanation box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Nord9,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = "💡 Corrección de Bessel: Al calcular la varianza de una muestra se divide entre (n - 1) en lugar de n para corregir el sesgo y obtener un estimador insesgado de la dispersión poblacional.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VarianceFormulaPanel(
    typeTitle: String,
    symbol: String,
    stdSymbol: String,
    divisorLabel: String,
    accentColor: Color,
    formula: String,
    substitution: String,
    varianceResult: String,
    stdDevFormula: String,
    stdDevResult: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    letterSpacing = 0.8.sp
                )
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = symbol,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = divisorLabel,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = accentColor.copy(alpha = 0.2f))

            // Variance Section
            Text(
                text = "Varianza:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = formula,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = substitution,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Result Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$symbol =",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = varianceResult,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }
            }

            HorizontalDivider(color = accentColor.copy(alpha = 0.2f))

            // Standard Deviation Section
            Text(
                text = "Desviación Estándar:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "$stdSymbol = √$symbol = $stdDevFormula",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$stdSymbol ≈",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = stdDevResult,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }
            }
        }
    }
}
