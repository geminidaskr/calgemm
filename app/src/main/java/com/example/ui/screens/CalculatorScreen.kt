package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.components.MathFormulaView
import com.example.ui.components.MathKeypad
import com.example.ui.graph.InteractiveGrapher
import com.example.ui.theme.*
import com.example.viewmodel.PrecalcViewModel

@Composable
fun CalculatorScreen(
    viewModel: PrecalcViewModel,
    onNavigateToSolver: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentExpr by viewModel.currentExpression.collectAsState()
    val functions by viewModel.functions.collectAsState()
    val viewport by viewModel.viewport.collectAsState()
    val analysis by viewModel.analysis.collectAsState()

    var showKeypad by remember { mutableStateOf(false) }
    var viewMode by remember { mutableIntStateOf(0) } // 0 = Bento Grid, 1 = Full Property List

    val presetFunctions = listOf(
        "x^2 - 4" to "Cuadrática",
        "(2x + 1)/(x - 2)" to "Racional",
        "sin(x)" to "Senoidal",
        "e^x - 3" to "Exponencial",
        "ln(x + 2)" to "Logarítmica",
        "sqrt(x + 4)" to "Radical",
        "abs(x^2 - 4)" to "Valor Absoluto"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Mode Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BentoTabPill(
                title = "BENTO GRID",
                isSelected = viewMode == 0,
                onClick = { viewMode = 0 },
                modifier = Modifier.weight(1f)
            )
            BentoTabPill(
                title = "ANÁLISIS COMPLETO",
                isSelected = viewMode == 1,
                onClick = { viewMode = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (viewMode == 0) {
                // Bento Grid View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bento Tile 1: Hero Function Display Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FUNCIÓN ACTUAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = Nord9
                                )

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Nord3.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = analysis?.functionType ?: "Precálculo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Nord4,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Math Expression Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Nord8.copy(alpha = 0.12f))
                                    .border(1.dp, Nord8.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .clickable { showKeypad = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                if (currentExpr.isEmpty()) {
                                    Text(
                                        text = "Toca para ingresar f(x) e.g. x² - 4",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                } else {
                                    MathFormulaView(
                                        formula = "f(x) = $currentExpr",
                                        fontSize = 22.sp,
                                        color = Nord8
                                    )
                                }
                            }


                            // Horizontal Presets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presetFunctions.forEach { (expr, _) ->
                                    val isSelected = currentExpr == expr
                                    Surface(
                                        onClick = { viewModel.setExpression(expr) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Nord8 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 10.dp)
                                        ) {
                                            Text(
                                                text = MathFormatter.formatToUnicode(expr),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Nord0 else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bento Tile 2: Live Graph Card (High-performance 2D Canvas)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp
                    ) {
                        InteractiveGrapher(
                            functions = functions,
                            viewport = viewport,
                            onViewportChange = { viewModel.updateViewport(it) },
                            analysis = analysis,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Bento Row: Two modular tiles side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tile A: Pasos / Procedimiento
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(156.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ListAlt,
                                        contentDescription = null,
                                        tint = Nord9,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "PASOS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp,
                                        color = Nord9
                                    )
                                }

                                Text(
                                    text = when {
                                        analysis?.roots?.isNotEmpty() == true -> "Raíces en ${analysis?.roots?.joinToString(", ") { "x=$it" }}"
                                        analysis?.verticalAsymptotes?.isNotEmpty() == true -> "Asíntotas en ${analysis?.verticalAsymptotes?.joinToString(", ") { "x=$it" }}"
                                        else -> "Análisis analítico de dominio y rango de f(x)..."
                                    },
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3
                                )

                                Surface(
                                    onClick = { viewMode = 1 },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().height(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "VER DETALLE",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Tile B: Tip / Dominio
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(156.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lightbulb,
                                        contentDescription = null,
                                        tint = Nord13,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "PROPIEDADES",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp,
                                        color = Nord13
                                    )
                                }

                                Text(
                                    text = "Dom: ${analysis?.domain ?: "ℝ"}\nRan: ${analysis?.range ?: "ℝ"}",
                                    fontSize = 11.5.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 3
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Nord10)
                                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Nord9)
                                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Nord8)
                                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }
                            }
                        }
                    }


                    // Bento Bottom Action Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Main Solve / Analyze Action Button
                        Button(
                            onClick = {
                                viewModel.analyzeCurrentFunction()
                                viewMode = 1
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Nord7,
                                contentColor = Nord0
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Nord7.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Outlined.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Resolver & Analizar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Secondary Action Button (Keypad Toggle)
                        Surface(
                            onClick = { showKeypad = !showKeypad },
                            shape = RoundedCornerShape(16.dp),
                            color = Nord10,
                            modifier = Modifier
                                .width(56.dp)
                                .fillMaxHeight()
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (showKeypad) Icons.Outlined.KeyboardHide else Icons.Outlined.Keyboard,
                                    contentDescription = "Teclado",
                                    tint = Nord6,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Shortcut Banner: Calculadora Estadística & Gráficos (At the bottom of the scroll)
                    Surface(
                        onClick = onNavigateToStatistics,
                        shape = RoundedCornerShape(20.dp),
                        color = Nord8.copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Nord8.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Nord8),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Analytics,
                                        contentDescription = "Estadística",
                                        tint = Nord0,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Calculadora Estadística & Gráficos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Barras, Histogramas, Polígonos, Sectores, Dispersión",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = "Abrir",
                                tint = Nord8,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                // Full Bento Analysis Cards List
                AnalysisDetailsView(analysis = analysis)
            }
        }

        // Virtual Keypad
        AnimatedVisibility(
            visible = showKeypad,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            MathKeypad(
                onInsert = { viewModel.appendToExpression(it) },
                onBackspace = { viewModel.backspaceExpression() },
                onClear = { viewModel.clearExpression() },
                onCalculate = {
                    viewModel.analyzeCurrentFunction()
                    showKeypad = false
                }
            )
        }
    }
}

@Composable
private fun BentoTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Nord8 else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Nord8 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isSelected) Nord0 else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AnalysisDetailsView(analysis: FunctionAnalysis?) {
    if (analysis == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Nord8)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANÁLISIS DE PROPIEDADES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Nord9
                    )
                    Spacer(Modifier.height(4.dp))
                    MathFormulaView(
                        formula = "f(x) = ${analysis.expression}",
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            BentoPropertyCard(
                title = "Tipo de Función",
                icon = Icons.Outlined.Category,
                badge = analysis.functionType,
                badgeColor = Nord8,
                description = "Naturaleza algebraica o trascendente de la función."
            )
        }

        item {
            BentoPropertyCard(
                title = "Dominio",
                icon = Icons.Outlined.Public,
                badge = analysis.domain,
                badgeColor = Nord14,
                description = "Conjunto de valores reales x donde no hay indeterminaciones."
            )
        }

        item {
            BentoPropertyCard(
                title = "Rango",
                icon = Icons.Outlined.BarChart,
                badge = analysis.range,
                badgeColor = Nord10,
                description = "Conjunto de valores que toma la función f(x)."
            )
        }

        item {
            val rootsText = if (analysis.roots.isEmpty()) "No tiene ceros reales" else analysis.roots.joinToString(", ") { "x = $it" }
            BentoPropertyCard(
                title = "Ceros / Raíces Reales (f(x) = 0)",
                icon = Icons.Outlined.Adjust,
                badge = rootsText,
                badgeColor = Nord11,
                description = "Puntos de corte con el eje horizontal X."
            )
        }

        item {
            val yIntText = analysis.yIntercept?.let { "y = $it en (0, $it)" } ?: "No definido"
            BentoPropertyCard(
                title = "Intercepto con el Eje Y",
                icon = Icons.Outlined.Place,
                badge = yIntText,
                badgeColor = Nord7,
                description = "Valor evaluado en el origen f(0)."
            )
        }

        item {
            BentoPropertyCard(
                title = "Simetría (Paridad)",
                icon = Icons.Outlined.Balance,
                badge = analysis.symmetry,
                badgeColor = Nord15,
                description = "Simetría respecto al eje Y (Par) o respecto al origen (Impar)."
            )
        }

        item {
            val vasText = if (analysis.verticalAsymptotes.isEmpty()) "Ninguna" else analysis.verticalAsymptotes.joinToString(", ") { "x = $it" }
            val hasText = if (analysis.horizontalAsymptotes.isEmpty()) "Ninguna" else analysis.horizontalAsymptotes.joinToString(", ") { "y = $it" }
            BentoPropertyCard(
                title = "Asíntotas",
                icon = Icons.Outlined.GridGoldenratio,
                badge = "Vert: $vasText | Horiz: $hasText",
                badgeColor = Nord12,
                description = "Rectas límites hacia las que tiende la curva f(x)."
            )
        }

        item {
            val extremaText = if (analysis.localExtrema.isEmpty()) "Sin extremos en el intervalo estándar"
            else analysis.localExtrema.joinToString("\n") { pt ->
                val type = if (pt.pointType == PointType.LOCAL_MAX) "Máximo Local" else "Mínimo Local"
                "$type: (${pt.x}, ${pt.y})"
            }
            BentoPropertyCard(
                title = "Puntos Críticos y Extremos Locales",
                icon = Icons.Outlined.Terrain,
                badge = extremaText,
                badgeColor = Nord13,
                description = "Puntos con derivada nula f'(x) = 0."
            )
        }

        item {
            BentoPropertyCard(
                title = "Comportamiento en el Infinito (Límites)",
                icon = Icons.Outlined.AllInclusive,
                badge = analysis.behaviorAtInfinity,
                badgeColor = Nord9,
                description = "Tendencia de f(x) cuando x → ±∞."
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BentoPropertyCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    badgeColor: Color,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = badgeColor
                )
            }

            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
