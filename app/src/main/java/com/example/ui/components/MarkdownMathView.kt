package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathFormatter
import com.example.ui.theme.*

/**
 * Normalizes alternative math delimiters like \( ... \) to $ ... $
 * and \[ ... \] to $$ ... $$ for universal LaTeX/KaTeX compatibility.
 */
fun normalizeMathDelimiters(input: String): String {
    if (input.isEmpty()) return input
    var s = input
    // Convert \[ and \] to $$
    s = s.replace("\\\\[", "$$")
        .replace("\\\\]", "$$")
        .replace("\\[", "$$")
        .replace("\\]", "$$")
    // Convert \( and \) to $
    s = s.replace("\\\\(", "$")
        .replace("\\\\)", "$")
        .replace("\\(", "$")
        .replace("\\)", "$")
    return s
}

/**
 * Universal Mathematical Text & Markdown Renderer Component (TextoMatematico).
 * Central reusable component across the entire app for displaying:
 * - AI Tutor responses with Markdown and KaTeX math
 * - Inverse function formulas f⁻¹(x) and step-by-step procedures
 * - Injectivity verification proofs and horizontal line tests
 * - Composition proofs (f ∘ f⁻¹)(x) and (f⁻¹ ∘ f)(x)
 * - Equation solver results and LaTeX fraction notations
 */
@Composable
fun TextoMatematico(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.5.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    inlineOnly: Boolean = false
) {
    if (text.isBlank()) return

    val isComplexMarkdown = remember(text, inlineOnly) {
        !inlineOnly && (
            text.contains("\n") ||
            text.contains("###") ||
            text.contains("##") ||
            text.contains("#") ||
            text.contains("---") ||
            text.contains("$$") ||
            text.contains("\\[") ||
            text.contains("|") ||
            text.startsWith("* ") ||
            text.startsWith("1. ") ||
            text.contains("\n* ") ||
            text.contains("\n1. ") ||
            text.contains("Paso ")
        )
    }

    if (isComplexMarkdown) {
        MarkdownMathView(
            markdownText = text,
            modifier = modifier,
            baseFontSize = fontSize,
            textColor = color
        )
    } else {
        val annotated = remember(text) { renderAnnotatedMathText(text) }
        Text(
            text = annotated,
            modifier = modifier,
            fontSize = fontSize,
            color = color,
            fontWeight = fontWeight,
            lineHeight = if (lineHeight != TextUnit.Unspecified) lineHeight else (fontSize.value * 1.35).sp
        )
    }
}

/**
 * High-performance Markdown & LaTeX renderer for Jetpack Compose.
 * Sanitizes and parses Markdown (headings, tables, lists, bold, steps, code blocks, callouts)
 * and LaTeX mathematical expressions ($...$ inline, $$...$$ block display)
 * styled in KaTeX/Photomath academic typography with the app's Nord dark theme.
 */
@Composable
fun MarkdownMathView(
    markdownText: String,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 13.5.sp,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val normalized = remember(markdownText) { normalizeMathDelimiters(markdownText) }
    val blocks = remember(normalized) { parseMarkdownBlocks(normalized) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    RenderHeading(block)
                }
                is MarkdownBlock.StepCard -> {
                    RenderStepCard(block, baseFontSize)
                }
                is MarkdownBlock.ExampleCard -> {
                    RenderExampleCard(block, baseFontSize)
                }
                is MarkdownBlock.Table -> {
                    RenderTable(block, baseFontSize)
                }
                is MarkdownBlock.MathDisplay -> {
                    RenderMathDisplayBlock(block)
                }
                is MarkdownBlock.BulletItem -> {
                    RenderBulletItem(block, baseFontSize, textColor)
                }
                is MarkdownBlock.NumberedItem -> {
                    RenderNumberedItem(block, baseFontSize, textColor)
                }
                is MarkdownBlock.Callout -> {
                    RenderCallout(block, baseFontSize)
                }
                is MarkdownBlock.CodeBlock -> {
                    RenderCodeBlock(block)
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    RenderParagraph(block, baseFontSize, textColor)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Block Composables
// ---------------------------------------------------------------------------

@Composable
private fun RenderHeading(block: MarkdownBlock.Heading) {
    val (fontSize, topPadding, color) = when (block.level) {
        1 -> Triple(18.sp, 10.dp, Nord8)
        2 -> Triple(16.sp, 8.dp, Nord8)
        3 -> Triple(14.5.sp, 6.dp, MaterialTheme.colorScheme.onSurface)
        else -> Triple(13.5.sp, 4.dp, Nord9)
    }

    Column(modifier = Modifier.padding(top = topPadding, bottom = 2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (block.level <= 3) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Nord8)
                )
            }
            Text(
                text = renderAnnotatedMathText(block.text),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun RenderStepCard(block: MarkdownBlock.StepCard, baseFontSize: TextUnit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Step Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Nord8),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${block.stepNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Nord0
                    )
                }
                Text(
                    text = renderAnnotatedMathText(block.title),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Math Formula inside Step
            if (block.mathExpression.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .border(
                            1.dp,
                            Nord8.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val formattedMath = MathFormatter.latexToUnicode(block.mathExpression)
                    Text(
                        text = formattedMath,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Nord8
                    )
                }
            }

            // Explanation
            if (block.explanation.isNotBlank()) {
                Text(
                    text = renderAnnotatedMathText(block.explanation),
                    fontSize = baseFontSize,
                    lineHeight = (baseFontSize.value * 1.45).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Rule (neutral link style)
            if (!block.ruleApplied.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Nord9.copy(alpha = 0.12f))
                        .border(1.dp, Nord9.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "📖 ${block.ruleApplied}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Nord9
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderExampleCard(block: MarkdownBlock.ExampleCard, baseFontSize: TextUnit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Nord9.copy(alpha = 0.09f))
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val cornerRadius = 16.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawRoundRect(
                    color = Nord9.copy(alpha = 0.5f),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = strokeWidth, pathEffect = pathEffect)
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Nord9.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = "Ejemplo",
                        tint = Nord9,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column {
                    Text(
                        text = "EJEMPLO ILUSTRATIVO",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = Nord9
                    )
                    if (block.title.isNotBlank() && !block.title.equals("Ejemplo", ignoreCase = true) && !block.title.equals("Ejemplo práctico", ignoreCase = true) && !block.title.equals("Ejemplo ilustrativo", ignoreCase = true)) {
                        Text(
                            text = renderAnnotatedMathText(block.title),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Math Expression in card if present
            if (!block.mathExpression.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .border(
                            1.dp,
                            Nord8.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = MathFormatter.latexToUnicode(block.mathExpression),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Nord8
                    )
                }
            }

            // Explanation / Content
            if (block.content.isNotBlank()) {
                Text(
                    text = renderAnnotatedMathText(block.content),
                    fontSize = baseFontSize,
                    lineHeight = (baseFontSize.value * 1.45).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RenderTable(block: MarkdownBlock.Table, baseFontSize: TextUnit) {
    val scrollState = rememberScrollState()
    val colCount = maxOf(block.headers.size, block.rows.maxOfOrNull { it.size } ?: 1)
    val alignments = List(colCount) { idx -> block.alignments.getOrElse(idx) { TextAlign.Start } }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = (colCount * 115).dp)
                    .fillMaxWidth()
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Nord8.copy(alpha = 0.18f))
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (colIdx in 0 until colCount) {
                        val headerText = block.headers.getOrElse(colIdx) { "" }
                        val alignment = alignments.getOrElse(colIdx) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            contentAlignment = when (alignment) {
                                TextAlign.Center -> Alignment.Center
                                TextAlign.End -> Alignment.CenterEnd
                                else -> Alignment.CenterStart
                            }
                        ) {
                            Text(
                                text = renderAnnotatedMathText(headerText),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Nord8,
                                textAlign = alignment
                            )
                        }
                        if (colIdx < colCount - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Nord8.copy(alpha = 0.35f))
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    thickness = 1.dp
                )

                // Table Data Rows
                block.rows.forEachIndexed { rowIdx, rowCells ->
                    val rowBg = if (rowIdx % 2 == 0) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (colIdx in 0 until colCount) {
                            val cellText = rowCells.getOrElse(colIdx) { "" }
                            val alignment = alignments.getOrElse(colIdx) { TextAlign.Start }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = when (alignment) {
                                    TextAlign.Center -> Alignment.Center
                                    TextAlign.End -> Alignment.CenterEnd
                                    else -> Alignment.CenterStart
                                }
                            ) {
                                Text(
                                    text = renderAnnotatedMathText(cellText),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = alignment
                                )
                            }
                            if (colIdx < colCount - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }

                    if (rowIdx < block.rows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderMathDisplayBlock(block: MarkdownBlock.MathDisplay) {
    val mathFormatted = MathFormatter.latexToUnicode(block.latex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                1.dp,
                Nord8.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mathFormatted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Nord8,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun RenderBulletItem(
    block: MarkdownBlock.BulletItem,
    fontSize: TextUnit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (block.indent * 12).dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Nord8,
            modifier = Modifier.padding(top = 1.dp)
        )
        Text(
            text = renderAnnotatedMathText(block.text),
            fontSize = fontSize,
            lineHeight = (fontSize.value * 1.45).sp,
            color = textColor
        )
    }
}

@Composable
private fun RenderNumberedItem(
    block: MarkdownBlock.NumberedItem,
    fontSize: TextUnit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (block.indent * 12).dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Nord8.copy(alpha = 0.15f),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = "${block.number}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Nord8,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
        }
        Text(
            text = renderAnnotatedMathText(block.text),
            fontSize = fontSize,
            lineHeight = (fontSize.value * 1.45).sp,
            color = textColor
        )
    }
}

@Composable
private fun RenderCallout(block: MarkdownBlock.Callout, fontSize: TextUnit) {
    val (bgColor, borderColor, icon, iconTint) = when (block.type) {
        CalloutType.TIP -> Quad(Nord13.copy(alpha = 0.12f), Nord13.copy(alpha = 0.4f), Icons.Outlined.Lightbulb, Nord13)
        CalloutType.NOTE -> Quad(Nord8.copy(alpha = 0.12f), Nord8.copy(alpha = 0.4f), Icons.Outlined.Info, Nord8)
        CalloutType.SUCCESS -> Quad(Nord14.copy(alpha = 0.12f), Nord14.copy(alpha = 0.4f), Icons.Outlined.CheckCircle, Nord14)
        CalloutType.WARNING -> Quad(Nord12.copy(alpha = 0.12f), Nord12.copy(alpha = 0.4f), Icons.Outlined.PushPin, Nord12)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 1.dp)
            )
            Text(
                text = renderAnnotatedMathText(block.text),
                fontSize = fontSize,
                lineHeight = (fontSize.value * 1.45).sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RenderCodeBlock(block: MarkdownBlock.CodeBlock) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Nord0.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Nord3.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = block.code,
            fontSize = 11.5.sp,
            fontFamily = FontFamily.Monospace,
            color = Nord8,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
private fun RenderParagraph(
    block: MarkdownBlock.Paragraph,
    fontSize: TextUnit,
    textColor: Color
) {
    Text(
        text = renderAnnotatedMathText(block.text),
        fontSize = fontSize,
        lineHeight = (fontSize.value * 1.45).sp,
        color = textColor
    )
}

// ---------------------------------------------------------------------------
// Inline Markdown & Math Parser (AnnotatedString)
// ---------------------------------------------------------------------------

/**
 * Parses inline formatting:
 * - **bold**
 * - *italic*
 * - `code`
 * - $latex math$ (rendered in KaTeX Serif academic math style)
 */
fun renderAnnotatedMathText(raw: String): AnnotatedString {
    val normalizedRaw = normalizeMathDelimiters(raw)
    return buildAnnotatedString {
        var cursor = 0
        val text = normalizedRaw.trim()

        while (cursor < text.length) {
            // Check inline math: $ ... $
            if (text[cursor] == '$') {
                val nextDollar = text.indexOf('$', cursor + 1)
                if (nextDollar != -1) {
                    val mathContent = text.substring(cursor + 1, nextDollar).trim()
                    val unicodeMath = MathFormatter.latexToUnicode(mathContent)

                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Nord8
                        )
                    )
                    append(" $unicodeMath ")
                    pop()

                    cursor = nextDollar + 1
                    continue
                }
            }

            // Check inline code: ` ... `
            if (text[cursor] == '`') {
                val nextBacktick = text.indexOf('`', cursor + 1)
                if (nextBacktick != -1) {
                    val codeContent = text.substring(cursor + 1, nextBacktick)
                    val unicodeCode = MathFormatter.latexToUnicode(codeContent)

                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Nord8,
                            background = Nord3.copy(alpha = 0.4f)
                        )
                    )
                    append(" $unicodeCode ")
                    pop()

                    cursor = nextBacktick + 1
                    continue
                }
            }

            // Check bold: ** ... **
            if (cursor + 1 < text.length && text[cursor] == '*' && text[cursor + 1] == '*') {
                val nextStarStar = text.indexOf("**", cursor + 2)
                if (nextStarStar != -1) {
                    val boldContent = text.substring(cursor + 2, nextStarStar)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(renderAnnotatedMathText(boldContent))
                    pop()

                    cursor = nextStarStar + 2
                    continue
                }
            }

            // Check italic: * ... * (single star)
            if (text[cursor] == '*' && (cursor == 0 || text[cursor - 1] != '*')) {
                val nextStar = text.indexOf('*', cursor + 1)
                if (nextStar != -1 && (nextStar + 1 >= text.length || text[nextStar + 1] != '*')) {
                    val italicContent = text.substring(cursor + 1, nextStar)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(renderAnnotatedMathText(italicContent))
                    pop()

                    cursor = nextStar + 1
                    continue
                }
            }

            // Check unescaped LaTeX macros: \frac, \sqrt, \left, \right, \implies, \circ, etc.
            if (text[cursor] == '\\' && cursor + 1 < text.length && (text[cursor + 1].isLetter() || text[cursor + 1] == '{' || text[cursor + 1] == '}')) {
                var endMacro = cursor + 1
                while (endMacro < text.length && text[endMacro].isLetter()) endMacro++
                val macroName = text.substring(cursor, endMacro)

                val complexMacros = listOf("\\frac", "\\dfrac", "\\tfrac", "\\sqrt", "\\mathbb", "\\text", "\\mathrm", "\\mathbf", "\\operatorname")
                if (macroName in complexMacros) {
                    val remaining = text.substring(cursor)
                    val converted = MathFormatter.latexToUnicode(remaining)
                    pushStyle(SpanStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, color = Nord8))
                    append(converted)
                    pop()
                    break
                } else {
                    val converted = MathFormatter.latexToUnicode(macroName)
                    pushStyle(SpanStyle(fontFamily = FontFamily.Serif, color = Nord8))
                    append(converted)
                    pop()
                    cursor = endMacro
                    continue
                }
            }

            // Check LaTeX superscript ^{...} or subscript _{...}
            if ((text[cursor] == '^' || text[cursor] == '_') && cursor + 1 < text.length && text[cursor + 1] == '{') {
                val closeBrace = text.indexOf('}', cursor + 2)
                if (closeBrace != -1) {
                    val braced = text.substring(cursor, closeBrace + 1)
                    val converted = MathFormatter.latexToUnicode(braced)
                    append(converted)
                    cursor = closeBrace + 1
                    continue
                }
            }

            // Append regular character
            append(text[cursor])
            cursor++
        }
    }
}

// ---------------------------------------------------------------------------
// Markdown AST Data Types and Parsing
// ---------------------------------------------------------------------------

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class StepCard(
        val stepNumber: Int,
        val title: String,
        val mathExpression: String,
        val explanation: String,
        val ruleApplied: String?
    ) : MarkdownBlock()
    data class ExampleCard(
        val title: String,
        val content: String,
        val mathExpression: String? = null
    ) : MarkdownBlock()
    data class Table(
        val headers: List<String>,
        val alignments: List<TextAlign>,
        val rows: List<List<String>>
    ) : MarkdownBlock()
    data class MathDisplay(val latex: String) : MarkdownBlock()
    data class BulletItem(val text: String, val indent: Int = 0) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String, val indent: Int = 0) : MarkdownBlock()
    data class Callout(val text: String, val type: CalloutType) : MarkdownBlock()
    data class CodeBlock(val code: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

enum class CalloutType {
    TIP, NOTE, SUCCESS, WARNING
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun isTableSeparatorLine(line: String): Boolean {
    val clean = line.trim()
    if (!clean.contains("-")) return false
    val rawCells = clean.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    if (rawCells.isEmpty()) return false
    return rawCells.all { cell ->
        cell.matches(Regex("""^:?-+:?$"""))
    }
}

private fun parseTableAlignments(sepLine: String, columnCount: Int): List<TextAlign> {
    val rawCells = sepLine.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    val list = rawCells.map { cell ->
        when {
            cell.startsWith(":") && cell.endsWith(":") -> TextAlign.Center
            cell.endsWith(":") -> TextAlign.End
            else -> TextAlign.Start
        }
    }
    return if (list.size < columnCount) {
        list + List(columnCount - list.size) { TextAlign.Start }
    } else list
}

private fun parseTableRowCells(line: String): List<String> {
    var raw = line.trim()
    if (raw.startsWith("|")) raw = raw.substring(1)
    if (raw.endsWith("|")) raw = raw.substring(0, raw.length - 1)
    return raw.split("|").map { it.trim() }
}

/**
 * Parses full raw markdown into structured blocks, recognizing step cards,
 * block math, callouts, and lists.
 */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val normalizedMarkdown = normalizeMathDelimiters(markdown)
    val lines = normalizedMarkdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.isBlank()) {
            i++
            continue
        }

        // 1. Divider
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // 1b. GFM Markdown Table
        if ((trimmed.startsWith("|") || trimmed.contains("|")) && i + 1 < lines.size) {
            val nextTrimmed = lines[i + 1].trim()
            if (isTableSeparatorLine(nextTrimmed)) {
                val headers = parseTableRowCells(trimmed)
                val alignments = parseTableAlignments(nextTrimmed, headers.size)
                i += 2 // skip header & separator
                val tableRows = mutableListOf<List<String>>()
                while (i < lines.size) {
                    val rLine = lines[i].trim()
                    if (rLine.isBlank() || !rLine.contains("|")) break
                    if (rLine.startsWith("#") || rLine == "---" || rLine.startsWith("$$")) break
                    val cells = parseTableRowCells(rLine)
                    if (cells.isNotEmpty()) {
                        tableRows.add(cells)
                    }
                    i++
                }
                if (headers.isNotEmpty() || tableRows.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Table(headers, alignments, tableRows))
                    continue
                }
            }
        }

        // 2. Display Block Math: $$ ... $$
        if (trimmed.startsWith("$$")) {
            if (trimmed.endsWith("$$") && trimmed.length > 4) {
                val inner = trimmed.removePrefix("$$").removeSuffix("$$").trim()
                blocks.add(MarkdownBlock.MathDisplay(inner))
                i++
                continue
            } else {
                // Multi-line block math
                val mathBuilder = StringBuilder()
                mathBuilder.append(trimmed.removePrefix("$$")).append("\n")
                i++
                while (i < lines.size && !lines[i].trim().endsWith("$$")) {
                    mathBuilder.append(lines[i]).append("\n")
                    i++
                }
                if (i < lines.size) {
                    mathBuilder.append(lines[i].trim().removeSuffix("$$"))
                    i++
                }
                blocks.add(MarkdownBlock.MathDisplay(mathBuilder.toString().trim()))
                continue
            }
        }

        // 3. Headings: #, ##, ###, ####
        if (trimmed.startsWith("#")) {
            val level = trimmed.takeWhile { it == '#' }.length
            val headingText = trimmed.removePrefix("#".repeat(level)).trim()
            blocks.add(MarkdownBlock.Heading(level, headingText))
            i++
            continue
        }

        // 4. Step Pattern check (e.g. **Paso 1: ...** or 1. **Paso 1: ...**)
        val stepMatch = Regex("""(?:\d+\.\s*)?\*\*(?:Paso|Step)\s*(\d+)[:\s]*(.*?)\*\*""", RegexOption.IGNORE_CASE).find(trimmed)
        if (stepMatch != null) {
            val stepNum = stepMatch.groupValues[1].toIntOrNull() ?: 1
            val stepTitle = stepMatch.groupValues[2].trim().ifBlank { "Paso $stepNum" }

            var mathExpr = ""
            val explanationBuilder = StringBuilder()
            var ruleApplied: String? = null

            i++
            while (i < lines.size) {
                val nextLine = lines[i].trim()
                if (nextLine.isBlank()) {
                    i++
                    continue
                }
                if (nextLine.startsWith("#") || nextLine.startsWith("---") ||
                    Regex("""(?:\d+\.\s*)?\*\*(?:Paso|Step)\s*\d+""", RegexOption.IGNORE_CASE).containsMatchIn(nextLine)
                ) {
                    break
                }

                if (nextLine.startsWith("$$") && nextLine.endsWith("$$")) {
                    mathExpr = nextLine.removePrefix("$$").removeSuffix("$$").trim()
                } else if (nextLine.startsWith("📌") || nextLine.contains("*Propiedad*:") || nextLine.contains("*Justificación*:")) {
                    ruleApplied = nextLine.replace("📌", "").replace("*Propiedad*:", "").replace("*Justificación*:", "").trim()
                } else {
                    if (explanationBuilder.isNotEmpty()) explanationBuilder.append(" ")
                    explanationBuilder.append(nextLine)
                }
                i++
            }

            blocks.add(
                MarkdownBlock.StepCard(
                    stepNumber = stepNum,
                    title = stepTitle,
                    mathExpression = mathExpr,
                    explanation = explanationBuilder.toString(),
                    ruleApplied = ruleApplied
                )
            )
            continue
        }

        // 4b. Example Card Check (e.g. **Ejemplo...**, ### Ejemplo..., Ejemplo: ...)
        val isExampleHeader = trimmed.startsWith("**Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("* **Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("- **Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("### Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("## Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("# Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("Ejemplo:", ignoreCase = true) ||
                trimmed.startsWith("Por ejemplo:", ignoreCase = true) ||
                trimmed.startsWith("Ejemplo ", ignoreCase = true) ||
                trimmed.startsWith("💡 **Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("📝 **Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("🔍 **Ejemplo", ignoreCase = true) ||
                trimmed.startsWith("> **Ejemplo", ignoreCase = true)

        if (isExampleHeader) {
            var rawTitle = trimmed
                .removePrefix("#").removePrefix("#").removePrefix("#").removePrefix(">").trim()
                .removePrefix("*").removePrefix("-").trim()
                .removePrefix("**").removeSuffix("**")
                .removePrefix("💡").removePrefix("📝").removePrefix("🔍").trim()
            val exampleTitle = if (rawTitle.isBlank()) "Ejemplo Práctico" else rawTitle

            var mathExpr: String? = null
            val contentBuilder = StringBuilder()

            i++
            while (i < lines.size) {
                val nextLine = lines[i].trim()
                if (nextLine.isBlank()) {
                    if (i + 1 < lines.size) {
                        val peek = lines[i + 1].trim()
                        if (peek.startsWith("#") || peek.startsWith("---") ||
                            Regex("""(?:\d+\.\s*)?\*\*(?:Paso|Step)\s*\d+""", RegexOption.IGNORE_CASE).containsMatchIn(peek) ||
                            peek.startsWith("**Ejemplo", ignoreCase = true) ||
                            peek.startsWith("### Ejemplo", ignoreCase = true) ||
                            peek.startsWith("Ejemplo:", ignoreCase = true)
                        ) {
                            i++
                            break
                        }
                    }
                    i++
                    continue
                }

                if (nextLine.startsWith("#") || nextLine.startsWith("---") ||
                    Regex("""(?:\d+\.\s*)?\*\*(?:Paso|Step)\s*\d+""", RegexOption.IGNORE_CASE).containsMatchIn(nextLine) ||
                    nextLine.startsWith("**Ejemplo", ignoreCase = true) ||
                    nextLine.startsWith("### Ejemplo", ignoreCase = true) ||
                    nextLine.startsWith("Ejemplo:", ignoreCase = true)
                ) {
                    break
                }

                if (nextLine.startsWith("$$") && nextLine.endsWith("$$")) {
                    mathExpr = nextLine.removePrefix("$$").removeSuffix("$$").trim()
                } else {
                    if (contentBuilder.isNotEmpty()) contentBuilder.append("\n")
                    contentBuilder.append(nextLine)
                }
                i++
            }

            blocks.add(
                MarkdownBlock.ExampleCard(
                    title = exampleTitle,
                    content = contentBuilder.toString().trim(),
                    mathExpression = mathExpr
                )
            )
            continue
        }

        // 5. Callouts: 💡, 📌, >, etc.
        if (trimmed.startsWith(">") || trimmed.startsWith("💡") || trimmed.startsWith("📌") || trimmed.startsWith("⚠️")) {
            val (type, content) = when {
                trimmed.startsWith("💡") -> Pair(CalloutType.TIP, trimmed.removePrefix("💡").trim())
                trimmed.startsWith("📌") -> Pair(CalloutType.NOTE, trimmed.removePrefix("📌").trim())
                trimmed.startsWith("⚠️") -> Pair(CalloutType.WARNING, trimmed.removePrefix("⚠️").trim())
                else -> Pair(CalloutType.NOTE, trimmed.removePrefix(">").trim())
            }
            blocks.add(MarkdownBlock.Callout(content, type))
            i++
            continue
        }

        // 6. Bullet lists: *, -, +
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("+ ")) {
            val indent = line.takeWhile { it.isWhitespace() }.length / 2
            val bulletText = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.BulletItem(bulletText, indent))
            i++
            continue
        }

        // 7. Numbered lists: 1. , 2. , etc.
        val numberedMatch = Regex("""^(\d+)[\.\)]\s+(.*)$""").find(trimmed)
        if (numberedMatch != null) {
            val num = numberedMatch.groupValues[1]
            val content = numberedMatch.groupValues[2]
            val indent = line.takeWhile { it.isWhitespace() }.length / 2
            blocks.add(MarkdownBlock.NumberedItem(num, content, indent))
            i++
            continue
        }

        // 8. Regular paragraph
        blocks.add(MarkdownBlock.Paragraph(trimmed))
        i++
    }

    return blocks
}
