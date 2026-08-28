package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.PrecalcCurriculum
import com.example.math.PrecalcTopic
import com.example.math.TopicCategory
import com.example.ui.components.MathFormulaView
import com.example.ui.components.MathStepCard
import com.example.ui.components.TextoMatematico
import com.example.ui.theme.*

@Composable
fun TopicExplorerScreen(
    onLoadFunction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTopic by remember { mutableStateOf<PrecalcTopic?>(null) }

    val topics = PrecalcCurriculum.topics

    if (selectedTopic != null) {
        TopicDetailView(
            topic = selectedTopic!!,
            onBack = { selectedTopic = null },
            onLoadFunction = onLoadFunction
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Bento Hero Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TEMARIO DE PRECÁLCULO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = Nord9
                )
                Text(
                    text = "Aprende Conceptos & Ejercicios",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Teoría rigurosa, fórmulas fundamentales y ejercicios resueltos paso a paso.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Topic List as Bento Cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(topics) { topic ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { selectedTopic = topic },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Nord8.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (topic.category) {
                                    TopicCategory.FUNCTIONS_AND_GRAPHS -> Icons.Outlined.Timeline
                                    TopicCategory.POLYNOMIAL_AND_RATIONAL -> Icons.Outlined.Functions
                                    TopicCategory.EXPONENTIAL_AND_LOGARITHMIC -> Icons.Outlined.ShowChart
                                    TopicCategory.TRIGONOMETRY -> Icons.Outlined.PieChart
                                    TopicCategory.TRANSFORMATIONS_AND_COMPOSITION -> Icons.Outlined.Transform
                                    TopicCategory.EQUATIONS_AND_INEQUALITIES -> Icons.Outlined.Calculate
                                    TopicCategory.STATISTICS_AND_PROBABILITY -> Icons.Outlined.Analytics
                                },
                                contentDescription = null,
                                tint = Nord8,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = topic.subtitle.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = Nord9
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = topic.summary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "Ver tema",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicDetailView(
    topic: PrecalcTopic,
    onBack: () -> Unit,
    onLoadFunction: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Regresar", tint = Nord8, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = topic.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Theory Section Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TEORÍA Y CONCEPTOS CLAVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Nord9
                    )
                    Text(
                        text = topic.theoryMarkdown,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Key Formulas
        item {
            Text(
                text = "FÓRMULAS FUNDAMENTALES",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = Nord9,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(topic.keyFormulas) { formula ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formula.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Nord8
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        MathFormulaView(
                            formula = formula.formula,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextoMatematico(
                        text = formula.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Practical Examples
        item {
            Text(
                text = "EJERCICIOS RESUELTOS PASO A PASO",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = Nord9,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(topic.examples) { ex ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ex.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (!ex.graphableFunction.isNullOrEmpty()) {
                            Surface(
                                onClick = { onLoadFunction(ex.graphableFunction) },
                                shape = RoundedCornerShape(10.dp),
                                color = Nord8,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = Nord0, modifier = Modifier.size(14.dp))
                                    Text("Graficar", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Nord0)
                                }
                            }
                        }
                    }

                    TextoMatematico(
                        text = ex.problem,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Nord8
                    )

                    ex.solutionSteps.forEach { step ->
                        MathStepCard(
                            stepNumber = step.stepNumber,
                            title = step.title,
                            mathExpression = step.mathExpression,
                            explanation = step.explanation,
                            ruleApplied = step.ruleApplied
                        )
                    }

                    // Final answer highlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Nord14.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        TextoMatematico(
                            text = "✅ Respuesta: ${ex.finalAnswer}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Nord14
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
