package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathFormatter
import com.example.ui.components.MathFormulaView
import com.example.ui.components.MathKeypad
import com.example.ui.components.MathStepCard
import com.example.ui.components.TextoMatematico
import com.example.ui.theme.*
import com.example.viewmodel.PrecalcViewModel

@Composable
fun InverseFunctionScreen(
    viewModel: PrecalcViewModel,
    onNavigateToGraph: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val inverseInput by viewModel.inverseFunctionInput.collectAsState()
    val solution by viewModel.inverseSolution.collectAsState()
    var showKeypad by remember { mutableStateOf(false) }

    val exampleFunctions = listOf(
        "(2x + 1)/(x - 3)" to "Racional",
        "3x - 5" to "Lineal",
        "sqrt(2x + 4)" to "Radical",
        "x^3 + 2" to "Cúbica",
        "2x + 7" to "Polinómica",
        "e^(2x)" to "Exponencial",
        "ln(x - 1)" to "Logarítmica",
        "(x + 4)/(2x - 1)" to "Homográfica"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bento Hero Input Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Nord8.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                tint = Nord8,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "FUNCIÓN A INVERTIR f(x)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = Nord9
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { showKeypad = !showKeypad },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (showKeypad) Icons.Outlined.KeyboardHide else Icons.Outlined.Keyboard,
                                contentDescription = "Teclado",
                                tint = Nord8,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.clearInverse() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Limpiar",
                                tint = Nord11,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Math Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    MathFormulaView(
                        formula = if (inverseInput.isEmpty()) "f(x) = ..." else "f(x) = $inverseInput",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Example Functions Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    exampleFunctions.forEach { (fn, label) ->
                        Surface(
                            onClick = {
                                viewModel.setInverseInput(fn)
                                viewModel.solveCurrentInverse()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = "$label: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Nord8
                                )
                                Text(
                                    text = MathFormatter.formatToUnicode(fn),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Optional Math Keypad Drawer
        AnimatedVisibility(
            visible = showKeypad,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                MathKeypad(
                    onInsert = { key ->
                        viewModel.appendToInverse(key)
                        viewModel.solveCurrentInverse()
                    },
                    onBackspace = {
                        viewModel.backspaceInverse()
                        viewModel.solveCurrentInverse()
                    },
                    onClear = {
                        viewModel.clearInverse()
                    },
                    onCalculate = {
                        viewModel.solveCurrentInverse()
                        showKeypad = false
                    }
                )
            }
        }

        // Solution and Pedagogical Steps Lazy Column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(2.dp)) }

            if (solution != null) {
                val sol = solution!!

                // 1. Result & Injectivity Card
                item {
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RESULTADO DE LA FUNCIÓN INVERSA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = Nord9
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sol.isBijective) Nord14.copy(alpha = 0.2f) else Nord13.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (sol.isBijective) "INVERTIBLE" else "RESTRINGIDA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sol.isBijective) Nord14 else Nord13,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Inverse Function Formula Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Nord8.copy(alpha = 0.12f))
                                    .border(1.dp, Nord8.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Función Inversa Obtenida:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Nord9
                                    )
                                    MathFormulaView(
                                        formula = sol.inverseFunctionExpression,
                                        fontSize = 20.sp,
                                        color = Nord8
                                    )
                                }
                            }

                            // Injectivity Verification Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = if (sol.isBijective) Nord14 else Nord13,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Verificación de Inyectividad (1 a 1)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    TextoMatematico(
                                        text = sol.algebraicProof,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 17.sp
                                    )

                                    TextoMatematico(
                                        text = "📐 ${sol.horizontalLineTestResult}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Nord9
                                    )
                                }
                            }

                            // Domain and Range Duality
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Dominio f(x)", fontSize = 10.sp, color = Nord9, fontWeight = FontWeight.Bold)
                                        TextoMatematico(text = sol.domainOriginal, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("(= Rango f⁻¹)", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Rango f(x)", fontSize = 10.sp, color = Nord9, fontWeight = FontWeight.Bold)
                                        TextoMatematico(text = sol.rangeOriginal, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("(= Dominio f⁻¹)", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }

                            // Composition Verification
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Demostración por Composición:", fontSize = 10.sp, color = Nord9, fontWeight = FontWeight.Bold)
                                    Text("• (f ∘ f⁻¹)(x) = " + MathFormatter.latexToUnicode(sol.compositionProofFofFinv.removePrefix("f(f^{-1}(x)) = ").removePrefix("f(f⁻¹(x)) = ")), fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("• (f⁻¹ ∘ f)(x) = " + MathFormatter.latexToUnicode(sol.compositionProofFinvOfF.removePrefix("f^{-1}(f(x)) = ").removePrefix("f⁻¹(f(x)) = ")), fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // Quick Action: Plot Function & Inverse
                            Button(
                                onClick = { onNavigateToGraph(sol.graphableOriginal) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Nord8, contentColor = Nord0)
                            ) {
                                Icon(Icons.Outlined.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Graficar Función en el Visualizador", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // 2. Step-by-Step Procedure Header
                item {
                    Text(
                        text = "PROCEDIMIENTO DETALLADO DE RESOLUCIÓN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Nord9,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }

                // 3. Step Cards
                items(sol.steps) { step ->
                    MathStepCard(
                        stepNumber = step.stepNumber,
                        title = step.title,
                        mathExpression = step.mathExpression,
                        explanation = step.explanation,
                        ruleApplied = step.ruleApplied
                    )
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                tint = Nord8,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Ingresa una función para calcular su inversa paso a paso",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
