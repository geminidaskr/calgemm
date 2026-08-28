package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathFormatter
import com.example.ui.theme.*

/**
 * High-fidelity mathematical formula component in Jetpack Compose.
 * Renders authentic stacked fractions, radical signs with vinculum overbar,
 * and high-clarity exponents.
 */
@Composable
fun MathFormulaView(
    formula: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isHeroDisplay: Boolean = false
) {
    val clean = formula.trim()

    // Check if it's an equation with an equals sign (e.g. f(x) = (x^2 - 1) / (x + 2))
    val equalsIdx = clean.indexOf('=')
    if (equalsIdx > 0 && equalsIdx < clean.length - 1) {
        val lhs = clean.substring(0, equalsIdx).trim()
        val rhs = clean.substring(equalsIdx + 1).trim()
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MathTokenNode(expr = lhs, fontSize = fontSize, color = color)
            Text(
                text = "=",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color
            )
            MathTokenNode(expr = rhs, fontSize = fontSize, color = color)
        }
        return
    }

    MathTokenNode(
        expr = clean,
        modifier = modifier,
        fontSize = fontSize,
        color = color
    )
}

@Composable
private fun MathTokenNode(
    expr: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    // Check if it's a top-level fraction
    val fractionPair = MathFormatter.splitTopLevelFraction(expr)
    if (fractionPair != null) {
        StackedFractionView(
            numerator = fractionPair.first,
            denominator = fractionPair.second,
            modifier = modifier,
            fontSize = fontSize,
            color = color
        )
        return
    }

    // Check if it starts with sqrt(...)
    if (expr.startsWith("sqrt(") && expr.endsWith(")")) {
        val inner = expr.substring(5, expr.length - 1)
        RadicalSquareRootView(
            radicand = inner,
            modifier = modifier,
            fontSize = fontSize,
            color = color
        )
        return
    }

    // Fallback: Unicode formatted expression with rich superscripts
    val formattedUnicode = MathFormatter.formatToUnicode(expr)
    Text(
        text = formattedUnicode,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Normal,
        color = color,
        lineHeight = (fontSize.value * 1.3).sp
    )
}

/**
 * Renders a true mathematical stacked vertical fraction with horizontal vinculum line.
 */
@Composable
fun StackedFractionView(
    numerator: String,
    denominator: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Numerator
        Text(
            text = MathFormatter.formatToUnicode(numerator),
            fontSize = (fontSize.value * 0.9).sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            color = color
        )

        // Fraction horizontal bar (Vinculum)
        Box(
            modifier = Modifier
                .padding(vertical = 3.dp)
                .height(1.5.dp)
                .fillMaxWidth(0.95f)
                .background(color)
        )

        // Denominator
        Text(
            text = MathFormatter.formatToUnicode(denominator),
            fontSize = (fontSize.value * 0.9).sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            color = color
        )
    }
}

/**
 * Renders a genuine square root radical sign with overbar covering the radicand.
 */
@Composable
fun RadicalSquareRootView(
    radicand: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Radical hook canvas
        Canvas(
            modifier = Modifier
                .width(14.dp)
                .height(28.dp)
        ) {
            val strokeWidth = 2.dp.toPx()
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(w * 0.1f, h * 0.55f)
                lineTo(w * 0.35f, h * 0.75f)
                lineTo(w * 0.75f, h * 0.15f)
                lineTo(w, h * 0.15f)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Radicand with top horizontal overbar vinculum
        Column(
            modifier = Modifier.padding(start = 1.dp)
        ) {
            // Overbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color)
            )
            // Inner content
            Text(
                text = MathFormatter.formatToUnicode(radicand),
                modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 4.dp),
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = color
            )
        }
    }
}

/**
 * Bento Grid style card container for displaying step-by-step math cards.
 */
@Composable
fun MathStepCard(
    stepNumber: Int,
    title: String,
    mathExpression: String,
    explanation: String,
    ruleApplied: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bento Step Badge
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Nord8),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Nord0
                    )
                }

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Math Expression View Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                MathFormulaView(
                    formula = mathExpression,
                    fontSize = 18.sp,
                    color = Nord8
                )
            }

            // Pedagogical explanation
            TextoMatematico(
                text = explanation,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Rule/Property highlight (neutral link style consistent with app)
            if (!ruleApplied.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Nord9.copy(alpha = 0.12f))
                        .border(1.dp, Nord9.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextoMatematico(
                        text = "📖 $ruleApplied",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Nord9
                    )
                }
            }
        }
    }
}
