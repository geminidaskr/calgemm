package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class KeypadMode {
    NUMERIC,
    ALGEBRA,
    PRECALC_TRIG
}

/**
 * Dedicated mathematical keyboard for rapid and intuitive function and equation input.
 */
@Composable
fun MathKeypad(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(KeypadMode.NUMERIC) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Mode Selector Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadTabButton(
                text = "123",
                isSelected = mode == KeypadMode.NUMERIC,
                onClick = { mode = KeypadMode.NUMERIC },
                modifier = Modifier.weight(1f)
            )
            KeypadTabButton(
                text = "x² / √ / f(x)",
                isSelected = mode == KeypadMode.ALGEBRA,
                onClick = { mode = KeypadMode.ALGEBRA },
                modifier = Modifier.weight(1f)
            )
            KeypadTabButton(
                text = "sin / cos / ln",
                isSelected = mode == KeypadMode.PRECALC_TRIG,
                onClick = { mode = KeypadMode.PRECALC_TRIG },
                modifier = Modifier.weight(1f)
            )
        }

        // Keypad Grid
        when (mode) {
            KeypadMode.NUMERIC -> NumericKeypadGrid(
                onInsert = onInsert,
                onBackspace = onBackspace,
                onClear = onClear,
                onCalculate = onCalculate
            )
            KeypadMode.ALGEBRA -> AlgebraKeypadGrid(
                onInsert = onInsert,
                onBackspace = onBackspace,
                onClear = onClear,
                onCalculate = onCalculate
            )
            KeypadMode.PRECALC_TRIG -> TrigKeypadGrid(
                onInsert = onInsert,
                onBackspace = onBackspace,
                onClear = onClear,
                onCalculate = onCalculate
            )
        }
    }
}

@Composable
private fun KeypadTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NumericKeypadGrid(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCalculate: () -> Unit
) {
    val rows = listOf(
        listOf("x", "x²", "(", ")", "C"),
        listOf("7", "8", "9", "÷", "^"),
        listOf("4", "5", "6", "×", "-"),
        listOf("1", "2", "3", "+", "√("),
        listOf("0", ".", "π", "⌫", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                row.forEach { key ->
                    KeyButton(
                        symbol = key,
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                "=" -> onCalculate()
                                "x²" -> onInsert("^2")
                                "×" -> onInsert("*")
                                "÷" -> onInsert("/")
                                "π" -> onInsert("pi")
                                else -> onInsert(key)
                            }
                        },
                        isAction = key in listOf("C", "⌫", "="),
                        isAccent = key == "=",
                        isVariable = key in listOf("x", "x²", "π"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlgebraKeypadGrid(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCalculate: () -> Unit
) {
    val rows = listOf(
        listOf("x", "y", "x²", "x³", "xⁿ"),
        listOf("√(", "∛(", "|x|", "1/x", "e"),
        listOf("(", ")", "^", "+", "-"),
        listOf("≤", "≥", "≠", "×", "÷"),
        listOf("C", "pi", "e^x", "⌫", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                row.forEach { key ->
                    KeyButton(
                        symbol = key,
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                "=" -> onCalculate()
                                "x²" -> onInsert("^2")
                                "x³" -> onInsert("^3")
                                "xⁿ" -> onInsert("^")
                                "1/x" -> onInsert("1/(")
                                "|x|" -> onInsert("abs(")
                                "∛(" -> onInsert("cbrt(")
                                "e^x" -> onInsert("e^(")
                                "×" -> onInsert("*")
                                "÷" -> onInsert("/")
                                else -> onInsert(key)
                            }
                        },
                        isAction = key in listOf("C", "⌫", "="),
                        isAccent = key == "=",
                        isVariable = key in listOf("x", "y", "e", "pi"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrigKeypadGrid(
    onInsert: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCalculate: () -> Unit
) {
    val rows = listOf(
        listOf("sin(", "cos(", "tan(", "ln(", "log("),
        listOf("asin(", "acos(", "atan(", "log₂(", "e^("),
        listOf("sec(", "csc(", "cot(", "abs(", "sqrt("),
        listOf("x", "(", ")", "+", "-"),
        listOf("C", "pi", "0", "⌫", "=")
    )

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                row.forEach { key ->
                    KeyButton(
                        symbol = key,
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                "=" -> onCalculate()
                                "log₂(" -> onInsert("log_2(")
                                "pi" -> onInsert("pi")
                                else -> onInsert(key)
                            }
                        },
                        isAction = key in listOf("C", "⌫", "="),
                        isAccent = key == "=",
                        isVariable = key in listOf("x", "pi"),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAction: Boolean = false,
    isAccent: Boolean = false,
    isVariable: Boolean = false
) {
    val backgroundColor = when {
        isAccent -> MaterialTheme.colorScheme.primary
        isAction -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        isVariable -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isAccent -> MaterialTheme.colorScheme.onPrimary
        isAction -> MaterialTheme.colorScheme.onErrorContainer
        isVariable -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (symbol == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Backspace,
                contentDescription = "Borrar",
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        } else if (symbol == "=") {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Calcular",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = symbol,
                fontSize = if (symbol.length > 3) 12.sp else 16.sp,
                fontWeight = if (isAccent || isVariable) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = textColor
            )
        }
    }
}
