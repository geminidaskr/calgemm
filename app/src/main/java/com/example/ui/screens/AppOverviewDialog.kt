package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.AppDestination
import com.example.ui.theme.*

@Composable
fun AppOverviewDialog(
    onDismiss: () -> Unit,
    onNavigateTo: (AppDestination) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
                .testTag("app_overview_dialog"),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Nord8.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Guía",
                                tint = Nord8,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Guía de Funcionalidades",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "MAPA COMPLETO DE CAPACIDADES",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = Nord9
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    // Resumen en 5 Puntos Clave
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Nord8.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Nord8.copy(alpha = 0.35f)),
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
                                        imageVector = Icons.Outlined.ElectricBolt,
                                        contentDescription = "Resumen",
                                        tint = Nord13,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "RESUMEN EJECUTIVO (5 PUNTOS CLAVE)",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                val keyPoints = listOf(
                                    "1. Graficador de Precálculo" to "Análisis algebraico completo de raíces, asíntotas, inversa y trazado táctil.",
                                    "2. Recomendador con IA" to "Selección automática entre 6 tipos de gráficos según el tipo de datos.",
                                    "3. Varianza Paso a Paso" to "Tabla de diferencias cuadráticas con fórmulas poblacionales y muestrales.",
                                    "4. Estadística & Frecuencias" to "Tabla por intervalos de clase, medidas descriptivas y regresión lineal.",
                                    "5. Tutor IA & Teclado Extendido" to "Demostraciones guiadas, historial activo y teclado matemático optimizado."
                                )

                                keyPoints.forEach { (title, desc) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Nord8
                                        )
                                        Column {
                                            Text(
                                                text = title,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = desc,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // MÓDULO 1: Precálculo y Graficador
                    item {
                        ModuleBentoCard(
                            iconEmoji = "📐",
                            icon = Icons.Outlined.Calculate,
                            title = "Precálculo y Graficador",
                            accentColor = Nord8,
                            actionLabel = "Abrir Graficador",
                            onAction = {
                                onDismiss()
                                onNavigateTo(AppDestination.CALCULATOR)
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GroupSection(
                                    title = "Análisis de Funciones",
                                    items = listOf(
                                        "Dominio y Rango" to "Determina conjuntos de definición e imagen automáticamente.",
                                        "Puntos Críticos" to "Detecta raíces, corte en Y y vértices.",
                                        "Asíntotas y Simetría" to "Traza asíntotas y clasifica funciones pares/impares.",
                                        "Función Inversa" to "Resuelve f⁻¹(x) paso a paso sobre y = x.",
                                        "Transformaciones" to "Muestra desplazamientos, compresiones y reflexiones."
                                    )
                                )

                                GroupSection(
                                    title = "Familias de Funciones",
                                    items = listOf(
                                        "Polinomios & Racionales" to "Resuelve potencias n y fracciones algebraicas.",
                                        "Trascendentes" to "Modela trigonométricas, exponenciales y logaritmos.",
                                        "Especiales" to "Grafica radicales continuos y valor absoluto."
                                    )
                                )

                                GroupSection(
                                    title = "Plano Cartesiano Interactivo",
                                    items = listOf(
                                        "Navegación Táctil" to "Desplazamiento multidireccional libre y zoom gestual.",
                                        "Controles Rápidos" to "Centrado en (0,0), escala π y D-Pad.",
                                        "Modo Trazador" to "Mide coordenadas (x, y) sobre la curva."
                                    )
                                )
                            }
                        }
                    }

                    // MÓDULO 2: Calculadora Estadística
                    item {
                        ModuleBentoCard(
                            iconEmoji = "📊",
                            icon = Icons.Outlined.Analytics,
                            title = "Calculadora Estadística",
                            accentColor = Nord14,
                            actionLabel = "Abrir Estadística",
                            onAction = {
                                onDismiss()
                                onNavigateTo(AppDestination.STATISTICS)
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SUBFUNCIONES Y SALIDAS CLAVE:",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = Nord14
                                )

                                val statTable = listOf(
                                    Triple("Gráficos IA", "Recomendación por afinidad", "Barras, Histograma, Polígono, Circular, Dispersión, Caja"),
                                    Triple("Varianza Paso a Paso", "Tabla de 3 columnas", "Desviaciones (xᵢ - x̄), sumatoria y Bessel (n - 1)"),
                                    Triple("Tendencia Central", "Medidas de resumen", "Media (x̄), Mediana (Me), Modas y muestra (n)"),
                                    Triple("Dispersión & Forma", "Variabilidad y sesgo", "Desv. Típica (s), Varianza (s²), CV%, Rango, Asimetría"),
                                    Triple("Posición & Límites", "Distribución por cuartiles", "Cuartiles (Q₁, Q₂, Q₃), IQR, Mín/Máx, Outliers"),
                                    Triple("Tabla Frecuencias", "Agrupación por clases", "Clases (k), xᵢ, fᵢ, Fᵢ, hᵢ, Hᵢ, % y grados (°)"),
                                    Triple("Regresión Bivariada", "Ajuste lineal pareado", "Recta y = mx + b, Pearson (r) y bondad R²")
                                )

                                statTable.forEach { (subf, op, res) ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = subf,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = op,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Nord14
                                                )
                                            }
                                            Text(
                                                text = res,
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // MÓDULO 3: Tutor IA (Gemini)
                    item {
                        ModuleBentoCard(
                            iconEmoji = "🤖",
                            icon = Icons.Outlined.AutoAwesome,
                            title = "Tutor IA (Gemini)",
                            accentColor = Nord13,
                            actionLabel = "Consultar Tutor",
                            onAction = {
                                onDismiss()
                                onNavigateTo(AppDestination.AI_TUTOR)
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GroupSection(
                                    title = "Asistencia en Precálculo",
                                    items = listOf(
                                        "Demostraciones Paso a Paso" to "Explicaciones guiadas de despejes y propiedades.",
                                        "Límites y Discontinuidades" to "Interpretación intuitiva del comportamiento asintótico."
                                    )
                                )

                                GroupSection(
                                    title = "Asistencia en Estadística",
                                    items = listOf(
                                        "Lectura de Resultados" to "Diagnóstico de sesgos, asimetría e hipótesis.",
                                        "Casos Prácticos" to "Conexión de conceptos abstractos con ejemplos reales."
                                    )
                                )
                            }
                        }
                    }

                    // MÓDULO 4: Utilidades y Accesibilidad
                    item {
                        ModuleBentoCard(
                            iconEmoji = "⚙️",
                            icon = Icons.Outlined.Settings,
                            title = "Utilidades y Accesibilidad",
                            accentColor = Nord9,
                            actionLabel = null,
                            onAction = null
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GroupSection(
                                    title = "Entrada de Datos",
                                    items = listOf(
                                        "Teclado Especializado" to "Acceso directo a potencias, fracciones y constantes.",
                                        "Presets Rápidos" to "Muestras predeterminadas para pruebas instantáneas."
                                    )
                                )

                                GroupSection(
                                    title = "Configuración y Registro",
                                    items = listOf(
                                        "Historial Activo" to "Recuperación inmediata de expresiones y cálculos.",
                                        "Temas Visuales" to "Paleta Nord optimizada en modos Claro/Oscuro."
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleBentoCard(
    iconEmoji: String,
    icon: ImageVector,
    title: String,
    accentColor: Color,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iconEmoji,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (actionLabel != null && onAction != null) {
                    Surface(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = actionLabel,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun GroupSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        items.forEach { (subTitle, desc) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "▪",
                    fontSize = 10.sp,
                    color = Nord8,
                    modifier = Modifier.padding(top = 1.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$subTitle:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
