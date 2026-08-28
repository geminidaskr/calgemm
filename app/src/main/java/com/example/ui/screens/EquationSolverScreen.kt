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
fun EquationSolverScreen(
    viewModel: PrecalcViewModel,
    onNavigateToGraph: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val equationInput by viewModel.equationInput.collectAsState()
    val solution by viewModel.equationSolution.collectAsState()
    var showKeypad by remember { mutableStateOf(false) }

    val exampleEquations = listOf(
        "x^2 - 5x + 6 = 0" to "Cuadrática",
        "2x^2 + 3x - 5 = 0" to "Fórmula General",
        "(2x + 1)/(x - 3) = 5" to "Racional",
        "sqrt(2x + 5) = 3" to "Radical",
        "e^(2x) = 7" to "Exponencial",
        "ln(x - 2) = 1" to "Logarítmica",
        "x^2 + 4 = 0" to "Compleja"
    )

    var solverMode by remember { mutableStateOf("ecuaciones") } // "ecuaciones" or "inversa"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Segmented Tab Selector Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Ecuaciones Tab
                Surface(
                    onClick = { solverMode = "ecuaciones" },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (solverMode == "ecuaciones") Nord8 else Color.Transparent,
                    shadowElevation = if (solverMode == "ecuaciones") 3.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Functions,
                            contentDescription = null,
                            tint = if (solverMode == "ecuaciones") Nord0 else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Ecuaciones",
                            fontSize = 12.sp,
                            fontWeight = if (solverMode == "ecuaciones") FontWeight.Bold else FontWeight.Medium,
                            color = if (solverMode == "ecuaciones") Nord0 else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Función Inversa Tab
                Surface(
                    onClick = { solverMode = "inversa" },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (solverMode == "inversa") Nord8 else Color.Transparent,
                    shadowElevation = if (solverMode == "inversa") 3.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                            tint = if (solverMode == "inversa") Nord0 else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Función Inversa",
                            fontSize = 12.sp,
                            fontWeight = if (solverMode == "inversa") FontWeight.Bold else FontWeight.Medium,
                            color = if (solverMode == "inversa") Nord0 else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (solverMode == "inversa") {
            InverseFunctionScreen(
                viewModel = viewModel,
                onNavigateToGraph = onNavigateToGraph,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
        // Bento Hero Equation Input Card
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
                    Text(
                        text = "ECUACIÓN A RESOLVER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Nord9
                    )

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
                            onClick = { viewModel.clearEquation() },
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

                // Math Formula Display of Equation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .clickable { showKeypad = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (equationInput.isEmpty()) {
                        Text(
                            text = "Ingresa una ecuación e.g. x² - 5x + 6 = 0",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        MathFormulaView(
                            formula = equationInput,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Quick Examples Horizontal Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    exampleEquations.forEach { (eq, _) ->
                        val isSelected = equationInput == eq
                        Surface(
                            onClick = {
                                viewModel.setEquationInput(eq)
                                viewModel.solveCurrentEquation()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Nord8 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = MathFormatter.formatToUnicode(eq),
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

        // Procedure List & Solutions
        Box(modifier = Modifier.weight(1f)) {
            if (solution == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Ingresa una ecuación para ver su procedimiento paso a paso.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                val currentSolution = solution!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Bento Solution Hero Banner
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    RoundedCornerShape(24.dp)
                                ),
                            color = MaterialTheme.colorScheme.surfaceVariant,
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
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Nord8.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = currentSolution.equationType.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp,
                                            color = Nord8,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    // Button to Plot in Grapher
                                    Surface(
                                        onClick = {
                                            val sides = equationInput.split("=")
                                            val plotExpr = if (sides.size > 1) "(${sides[0]}) - (${sides[1]})" else equationInput
                                            onNavigateToGraph(plotExpr)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Nord8,
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = Nord0, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "Graficar",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Nord0
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "SOLUCIÓN FINAL",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = Nord9
                                )

                                TextoMatematico(
                                    text = currentSolution.solutions.joinToString("   o   "),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Nord8
                                )

                            }
                        }
                    }

                    item {
                        Text(
                            text = "PROCEDIMIENTO PASO A PASO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = Nord9,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(currentSolution.steps) { step ->
                        MathStepCard(
                            stepNumber = step.stepNumber,
                            title = step.title,
                            mathExpression = step.mathExpression,
                            explanation = step.explanation,
                            ruleApplied = step.ruleApplied
                        )
                    }

                    if (!currentSolution.notes.isNullOrEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Nord13, modifier = Modifier.size(20.dp))
                                    TextoMatematico(
                                        text = currentSolution.notes,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // Virtual Keypad
        AnimatedVisibility(
            visible = showKeypad,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            MathKeypad(
                onInsert = { viewModel.appendToEquation(it) },
                onBackspace = { viewModel.backspaceEquation() },
                onClear = { viewModel.clearEquation() },
                onCalculate = {
                    viewModel.solveCurrentEquation()
                    showKeypad = false
                }
            )
        }
    }
}
}
}
