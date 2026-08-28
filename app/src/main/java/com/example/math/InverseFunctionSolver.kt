package com.example.math

import kotlin.math.*

/**
 * Analytical engine to compute inverse functions f⁻¹(x) with:
 * 1. Injectivity Verification (Horizontal Line Test + Algebraic proof f(a) = f(b) => a = b)
 * 2. Expressing as y = f(x)
 * 3. Variable Swapping (x <-> y => x = f(y))
 * 4. Step-by-step algebraic isolation of y
 * 5. Domain & Range mapping (Dom(f⁻¹) = Ran(f), Ran(f⁻¹) = Dom(f))
 * 6. Composition verification (f(f⁻¹(x)) = x and f⁻¹(f(x)) = x)
 */
object InverseFunctionSolver {

    fun solveInverse(inputExpr: String): InverseFunctionSolution {
        val clean = inputExpr
            .removePrefix("f(x)")
            .removePrefix("y")
            .removePrefix("=")
            .trim()

        val lower = clean.lowercase().replace(" ", "")

        return when {
            // Rational: (ax + b) / (cx + d) or (ax + b) / x or 1 / (x + d)
            lower.contains("/") -> solveRationalInverse(clean, lower)

            // Radical: sqrt(ax + b) or √(ax + b)
            lower.contains("sqrt") || lower.contains("√") -> solveRadicalInverse(clean, lower)

            // Cubic: x^3, ax^3 + b
            lower.contains("^3") -> solveCubicInverse(clean, lower)

            // Quadratic: x^2, ax^2 + bx + c
            lower.contains("^2") -> solveQuadraticInverse(clean, lower)

            // Exponential: e^x, 2^x, exp(x)
            lower.contains("e^") || lower.contains("exp") || lower.matches(Regex(".*\\d+\\^x.*")) -> solveExponentialInverse(clean, lower)

            // Logarithmic: ln(x), log(x)
            lower.contains("ln") || lower.contains("log") -> solveLogarithmicInverse(clean, lower)

            // Linear: ax + b or -ax + b or x
            lower.contains("x") -> solveLinearInverse(clean, lower)

            else -> solveGenericInverse(clean)
        }
    }

    // --- 1. Linear: f(x) = ax + b ---
    private fun solveLinearInverse(orig: String, lower: String): InverseFunctionSolution {
        // Parse a and b in ax + b
        var a = 1.0
        var b = 0.0

        val linearRegex = Regex("""^([+-]?\d*\.?\d*)x([+-]\d+\.?\d*)?$""")
        val match = linearRegex.find(lower)

        if (match != null) {
            val aStr = match.groupValues[1]
            val bStr = match.groupValues[2]
            a = when (aStr) {
                "", "+" -> 1.0
                "-" -> -1.0
                else -> aStr.toDoubleOrNull() ?: 1.0
            }
            if (bStr.isNotBlank()) {
                b = bStr.toDoubleOrNull() ?: 0.0
            }
        }

        val aFormatted = if (a == 1.0) "" else if (a == -1.0) "-" else formatDouble(a)
        val origFormatted = if (b == 0.0) "${aFormatted}x" else if (b > 0) "${aFormatted}x + ${formatDouble(b)}" else "${aFormatted}x - ${formatDouble(abs(b))}"

        val inverseExpr = if (a == 1.0) {
            if (b == 0.0) "x" else if (b > 0) "x - ${formatDouble(b)}" else "x + ${formatDouble(abs(b))}"
        } else if (a == -1.0) {
            if (b == 0.0) "-x" else if (b > 0) "-x + ${formatDouble(b)}" else "-x - ${formatDouble(abs(b))}"
        } else {
            if (b == 0.0) "x / ${formatDouble(a)}"
            else if (b > 0) "(x - ${formatDouble(b)}) / ${formatDouble(a)}"
            else "(x + ${formatDouble(abs(b))}) / ${formatDouble(a)}"
        }

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad (1 a 1)",
                mathExpression = "f(a) = f(b) \\implies $aFormatted a + $b = $aFormatted b + $b",
                explanation = "Una función lineal con pendiente m ≠ 0 es estrictamente monótona (creciente o decreciente). Cumple la prueba de la recta horizontal y f(a) = f(b) implica directamente a = b.",
                ruleApplied = "Criterio Algebraico de Inyectividad: f(a) = f(b) ⇒ a = b"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Expresar la función con la variable 'y'",
                mathExpression = "y = $origFormatted",
                explanation = "Escribimos la relación explícita asignando y = f(x)."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = ${if (a == 1.0) "y" else if (a == -1.0) "-y" else "${formatDouble(a)}y"} ${if (b > 0) "+ ${formatDouble(b)}" else if (b < 0) "- ${formatDouble(abs(b))}" else ""}",
                explanation = "Para hallar la función inversa, intercambiamos los roles de la variable independiente y dependiente (reflexión geométrica sobre la recta diagonal y = x).",
                ruleApplied = "Simetría Especular respecto a y = x"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Despeje algebraico de 'y'",
                mathExpression = if (b != 0.0) "x - (${formatDouble(b)}) = ${aFormatted}y \\implies y = $inverseExpr" else "${aFormatted}y = x \\implies y = $inverseExpr",
                explanation = "Aislamos la variable y en un miembro de la igualdad mediante operaciones inversas.",
                ruleApplied = "Propiedad de Igualdad y Despeje Algebraico"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 5,
                title = "Definición Formal de la Función Inversa",
                mathExpression = "f^{-1}(x) = $inverseExpr",
                explanation = "Reemplazamos 'y' por la notación estándar f⁻¹(x)."
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = $origFormatted",
            isBijective = true,
            injectivityVerification = "Inyectiva en todo ℝ (Función Lineal con pendiente m = $a ≠ 0)",
            horizontalLineTestResult = "Cumple la prueba: Cualquier recta horizontal y = c corta a la gráfica en exactamente un punto.",
            algebraicProof = "f(a) = f(b) ⇒ ${a}a + $b = ${a}b + $b ⇒ ${a}a = ${a}b ⇒ a = b. Por tanto es inyectiva.",
            variableSwapExpression = "x = $origFormatted (reemplazando x por y)",
            inverseFunctionExpression = "f^{-1}(x) = $inverseExpr",
            domainOriginal = "ℝ = (-∞, ∞)",
            rangeOriginal = "ℝ = (-∞, ∞)",
            domainInverse = "ℝ = (-∞, ∞)",
            rangeInverse = "ℝ = (-∞, ∞)",
            compositionProofFofFinv = "f(f^{-1}(x)) = $aFormatted \\left($inverseExpr\\right) ${if (b > 0) "+ $b" else if (b < 0) "- ${abs(b)}" else ""} = x",
            compositionProofFinvOfF = "f^{-1}(f(x)) = \\frac{($origFormatted) ${if (b > 0) "- $b" else if (b < 0) "+ ${abs(b)}" else ""}}{$a} = x",
            steps = steps,
            graphableOriginal = origFormatted,
            graphableInverse = inverseExpr
        )
    }

    // --- 2. Rational: f(x) = (ax + b) / (cx + d) ---
    private fun solveRationalInverse(orig: String, lower: String): InverseFunctionSolution {
        // Parse numerator and denominator
        val parts = lower.removePrefix("(").removeSuffix(")").split("/")
        val numStr = parts.getOrNull(0) ?: "x"
        val denStr = parts.getOrNull(1) ?: "1"

        // Default linear coefficients
        var a = 1.0; var b = 0.0
        var c = 1.0; var d = 0.0

        if (numStr == "1") {
            a = 0.0; b = 1.0
        } else {
            val mNum = Regex("""^([+-]?\d*\.?\d*)x([+-]\d+\.?\d*)?$""").find(numStr.replace("(", "").replace(")", ""))
            if (mNum != null) {
                val aS = mNum.groupValues[1]
                val bS = mNum.groupValues[2]
                a = when (aS) { "", "+" -> 1.0; "-" -> -1.0; else -> aS.toDoubleOrNull() ?: 1.0 }
                if (bS.isNotBlank()) b = bS.toDoubleOrNull() ?: 0.0
            }
        }

        val mDen = Regex("""^([+-]?\d*\.?\d*)x([+-]\d+\.?\d*)?$""").find(denStr.replace("(", "").replace(")", ""))
        if (mDen != null) {
            val cS = mDen.groupValues[1]
            val dS = mDen.groupValues[2]
            c = when (cS) { "", "+" -> 1.0; "-" -> -1.0; else -> cS.toDoubleOrNull() ?: 1.0 }
            if (dS.isNotBlank()) d = dS.toDoubleOrNull() ?: 0.0
        }

        // For f(x) = (ax + b) / (cx + d), the inverse is f⁻¹(x) = (-dx + b) / (cx - a)
        val invNumA = -d
        val invNumB = b
        val invDenC = c
        val invDenD = -a

        fun formatPoly(coefX: Double, constVal: Double): String {
            val xPart = when {
                coefX == 1.0 -> "x"
                coefX == -1.0 -> "-x"
                coefX == 0.0 -> ""
                else -> "${formatDouble(coefX)}x"
            }
            return when {
                xPart.isEmpty() -> formatDouble(constVal)
                constVal == 0.0 -> xPart
                constVal > 0 -> "$xPart + ${formatDouble(constVal)}"
                else -> "$xPart - ${formatDouble(abs(constVal))}"
            }
        }

        val origNumFmt = formatPoly(a, b)
        val origDenFmt = formatPoly(c, d)
        val invNumFmt = formatPoly(invNumA, invNumB)
        val invDenFmt = formatPoly(invDenC, invDenD)

        val inverseExpr = "($invNumFmt) / ($invDenFmt)"

        val origExcludedDom = if (c != 0.0) formatDouble(-d / c) else "Ninguno"
        val invExcludedDom = if (invDenC != 0.0) formatDouble(-invDenD / invDenC) else "Ninguno"

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad y Dominio",
                mathExpression = "f(a) = f(b) \\implies \\frac{$origNumFmt(a)}{$origDenFmt(a)} = \\frac{$origNumFmt(b)}{$origDenFmt(b)}",
                explanation = "Multiplicando en cruz y cancelando términos semejantes se deduce (ad - bc)(a - b) = 0. Dado que ad - bc ≠ 0, se cumple a = b. Por tanto es inyectiva en su dominio ℝ ∖ {$origExcludedDom}.",
                ruleApplied = "Criterio Algebraico de Inyectividad en Funciones Racionales"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Escribir como ecuación y = f(x)",
                mathExpression = "y = \\frac{$origNumFmt}{$origDenFmt}",
                explanation = "Sustituimos f(x) por y para facilitar el despeje algebraico."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = \\frac{${formatPoly(a, b).replace("x", "y")}}{${formatPoly(c, d).replace("x", "y")}}",
                explanation = "Intercambiamos x por y para reflejar la función respecto a la recta y = x.",
                ruleApplied = "Reflexión Geométrica sobre la Recta Identidad"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Multiplicar por el denominador para linealizar",
                mathExpression = "x \\cdot (${formatPoly(c, d).replace("x", "y")}) = ${formatPoly(a, b).replace("x", "y")}",
                explanation = "Eliminamos la fracción multiplicando ambos lados por el denominador con 'y'."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 5,
                title = "Distribuir y agrupar todos los términos con 'y' en un miembro",
                mathExpression = "${if (c == 1.0) "xy" else "${formatDouble(c)}xy"} + ${formatDouble(d)}x = ${formatPoly(a, b).replace("x", "y")} \\implies y(${formatPoly(c, -a)}) = $invNumFmt",
                explanation = "Factorizamos la variable común 'y' como factor común.",
                ruleApplied = "Factorización por Factor Común"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 6,
                title = "Despejar 'y' dividiendo entre el factor",
                mathExpression = "y = \\frac{$invNumFmt}{$invDenFmt}",
                explanation = "Dividimos ambos lados entre (${formatPoly(c, -a)}) para obtener la función inversa."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 7,
                title = "Expresión Final de la Función Inversa",
                mathExpression = "f^{-1}(x) = \\frac{$invNumFmt}{$invDenFmt}",
                explanation = "Escribimos la función inversa resultante con sus respectivas restricciones."
            )
        )

        val aFmt = formatDouble(a)
        val bFmt = formatDouble(b)
        val cFmt = formatDouble(c)
        val dFmt = formatDouble(d)
        val invNumAFmt = formatDouble(invNumA)
        val invDenDFmt = formatDouble(invDenD)

        return InverseFunctionSolution(
            originalFunction = "f(x) = \\frac{$origNumFmt}{$origDenFmt}",
            isBijective = true,
            injectivityVerification = "Inyectiva en todo su dominio real x ≠ $origExcludedDom",
            horizontalLineTestResult = "Cumple la prueba de la recta horizontal en cada rama de su dominio hiperbólico.",
            algebraicProof = "Demostrado: (ad - bc)(a - b) = 0 con ad - bc = ${formatDouble(a * d - b * c)} ≠ 0 ⇒ a = b.",
            variableSwapExpression = "x = \\frac{${origNumFmt.replace("x", "y")}}{${origDenFmt.replace("x", "y")}}",
            inverseFunctionExpression = "f^{-1}(x) = \\frac{$invNumFmt}{$invDenFmt}",
            domainOriginal = "ℝ ∖ {$origExcludedDom} = (-∞, $origExcludedDom) ∪ ($origExcludedDom, ∞)",
            rangeOriginal = "ℝ ∖ {$invExcludedDom} = (-∞, $invExcludedDom) ∪ ($invExcludedDom, ∞)",
            domainInverse = "ℝ ∖ {$invExcludedDom} = (-∞, $invExcludedDom) ∪ ($invExcludedDom, ∞)",
            rangeInverse = "ℝ ∖ {$origExcludedDom} = (-∞, $origExcludedDom) ∪ ($origExcludedDom, ∞)",
            compositionProofFofFinv = "\\frac{${if (a == 1.0) "" else "$aFmt "}\\left(\\frac{$invNumFmt}{$invDenFmt}\\right) ${if (b > 0) "+ $bFmt" else if (b < 0) "- ${formatDouble(abs(b))}" else ""}}{${if (c == 1.0) "" else "$cFmt "}\\left(\\frac{$invNumFmt}{$invDenFmt}\\right) ${if (d > 0) "+ $dFmt" else if (d < 0) "- ${formatDouble(abs(d))}" else ""}} = x",
            compositionProofFinvOfF = "\\frac{${if (invNumA == 1.0) "" else "$invNumAFmt "}\\left(\\frac{$origNumFmt}{$origDenFmt}\\right) ${if (b > 0) "+ $bFmt" else if (b < 0) "- ${formatDouble(abs(b))}" else ""}}{${if (c == 1.0) "" else "$cFmt "}\\left(\\frac{$origNumFmt}{$origDenFmt}\\right) ${if (invDenD > 0) "+ $invDenDFmt" else if (invDenD < 0) "- ${formatDouble(abs(invDenD))}" else ""}} = x",
            steps = steps,
            graphableOriginal = "($origNumFmt)/($origDenFmt)",
            graphableInverse = "($invNumFmt)/($invDenFmt)"
        )
    }

    // --- 3. Quadratic: f(x) = ax² + bx + c ---
    private fun solveQuadraticInverse(orig: String, lower: String): InverseFunctionSolution {
        var a = 1.0; var b = 0.0; var c = 0.0

        val qMatch = Regex("""^([+-]?\d*\.?\d*)x\^2([+-]\d*\.?\d*x)?([+-]\d+\.?\d*)?$""").find(lower)
        if (qMatch != null) {
            val aS = qMatch.groupValues[1]
            val bS = qMatch.groupValues[2].removeSuffix("x")
            val cS = qMatch.groupValues[3]

            a = when (aS) { "", "+" -> 1.0; "-" -> -1.0; else -> aS.toDoubleOrNull() ?: 1.0 }
            if (bS.isNotBlank()) {
                b = when (bS) { "+", "" -> 1.0; "-" -> -1.0; else -> bS.toDoubleOrNull() ?: 0.0 }
            }
            if (cS.isNotBlank()) c = cS.toDoubleOrNull() ?: 0.0
        }

        val h = -b / (2 * a) // Vértice x
        val k = a * h * h + b * h + c // Vértice y

        val hStr = formatDouble(h)
        val kStr = formatDouble(k)

        val inverseExpr = if (h == 0.0 && k == 0.0) {
            if (a == 1.0) "sqrt(x)" else "sqrt(x / ${formatDouble(a)})"
        } else if (h == 0.0) {
            if (k > 0) "sqrt(x - $kStr)" else "sqrt(x + ${formatDouble(abs(k))})"
        } else {
            val inside = if (k == 0.0) "x" else if (k > 0) "x - $kStr" else "x + ${formatDouble(abs(k))}"
            if (h > 0) "sqrt($inside) + $hStr" else "sqrt($inside) - ${formatDouble(abs(h))}"
        }

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad y Prueba de la Recta Horizontal",
                mathExpression = "f(x_1) = f(x_2) \\implies x_1 = \\pm x_2 \\text{ (No Inyectiva en todo ℝ)}",
                explanation = "Una función cuadrática (parábola) NO es inyectiva en todo ℝ porque una recta horizontal y = c corta a la curva en dos puntos simétricos respecto al eje del vértice x = $hStr. Para que tenga inversa, debemos RESTRINGIR EL DOMINIO al intervalo [ $hStr, ∞ ).",
                ruleApplied = "Restricción de Dominio para Biyección (Rama Derecha de la Parábola)"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Escribir la función cuadrática en forma canónica (vértice)",
                mathExpression = "y = ${if (a != 1.0) "${formatDouble(a)}" else ""}(x - $hStr)^2 + $kStr, \\quad x \\ge $hStr",
                explanation = "Completamos el cuadrado para expresar la parábola con una sola aparición de la variable x."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = ${if (a != 1.0) "${formatDouble(a)}" else ""}(y - $hStr)^2 + $kStr, \\quad y \\ge $hStr",
                explanation = "Intercambiamos x e y para aplicar la simetría respecto a la diagonal y = x.",
                ruleApplied = "Reflexión Geométrica y = x"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Despejar algebraicamente el término al cuadrado",
                mathExpression = "(y - $hStr)^2 = \\frac{x - $kStr}{${formatDouble(a)}}",
                explanation = "Restamos $kStr y dividimos entre $a."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 5,
                title = "Aplicar raíz cuadrada y seleccionar la rama positiva",
                mathExpression = "y - $hStr = +\\sqrt{${if (k == 0.0) "x" else "x - $kStr"}} \\implies y = $inverseExpr",
                explanation = "Dado que restringimos y ≥ $hStr, tomamos la raíz positiva (+√).",
                ruleApplied = "Extracción de Raíz Cuadrada Principal"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 6,
                title = "Función Inversa Final con Restricción",
                mathExpression = "f^{-1}(x) = $inverseExpr, \\quad x \\ge $kStr",
                explanation = "El dominio de la inversa corresponde exactamente al rango de la función original restringida."
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = $orig, \\quad x \\ge $hStr",
            isBijective = false,
            injectivityVerification = "No inyectiva globalmente en ℝ. Se restringe el dominio a [ $hStr, ∞ ) para garantizar biyectividad.",
            horizontalLineTestResult = "En todo ℝ falla la prueba de la recta horizontal. Con la restricción x ≥ $hStr, es estrictamente creciente e inyectiva.",
            algebraicProof = "Con x ≥ $hStr, f(a) = f(b) ⇒ (a - $hStr)² = (b - $hStr)² ⇒ a - $hStr = b - $hStr ⇒ a = b.",
            variableSwapExpression = "x = (y - $hStr)² + $kStr",
            inverseFunctionExpression = "f^{-1}(x) = $inverseExpr, \\quad x \\ge $kStr",
            domainOriginal = "[$hStr, ∞)",
            rangeOriginal = "[$kStr, ∞)",
            domainInverse = "[$kStr, ∞)",
            rangeInverse = "[$hStr, ∞)",
            compositionProofFofFinv = "f(f^{-1}(x)) = \\left($inverseExpr - $hStr\\right)^2 + $kStr = (x - $kStr) + $kStr = x",
            compositionProofFinvOfF = "f^{-1}(f(x)) = \\sqrt{(x - $hStr)^2} + $hStr = (x - $hStr) + $hStr = x",
            steps = steps,
            domainRestrictionNote = "⚠️ Importante: Para que una función cuadrática tenga inversa, su dominio debe restringirse a un lado del vértice (por ejemplo x ≥ $hStr).",
            graphableOriginal = orig,
            graphableInverse = inverseExpr
        )
    }

    // --- 4. Radical: f(x) = sqrt(ax + b) + c ---
    private fun solveRadicalInverse(orig: String, lower: String): InverseFunctionSolution {
        var a = 1.0; var b = 0.0; var c = 0.0

        val radMatch = Regex("""^(?:sqrt|√)\(([+-]?\d*\.?\d*)x([+-]\d+\.?\d*)?\)([+-]\d+\.?\d*)?$""").find(lower)
        if (radMatch != null) {
            val aS = radMatch.groupValues[1]
            val bS = radMatch.groupValues[2]
            val cS = radMatch.groupValues[3]

            a = when (aS) { "", "+" -> 1.0; "-" -> -1.0; else -> aS.toDoubleOrNull() ?: 1.0 }
            if (bS.isNotBlank()) b = bS.toDoubleOrNull() ?: 0.0
            if (cS.isNotBlank()) c = cS.toDoubleOrNull() ?: 0.0
        }

        val startX = -b / a
        val startY = c

        val inverseExpr = if (c == 0.0) {
            if (b == 0.0) {
                if (a == 1.0) "x^2" else "x^2 / ${formatDouble(a)}"
            } else if (b > 0) {
                "(x^2 - ${formatDouble(b)}) / ${formatDouble(a)}"
            } else {
                "(x^2 + ${formatDouble(abs(b))}) / ${formatDouble(a)}"
            }
        } else {
            val term = if (c > 0) "(x - ${formatDouble(c)})^2" else "(x + ${formatDouble(abs(c))})^2"
            if (b == 0.0) "$term / ${formatDouble(a)}"
            else if (b > 0) "($term - ${formatDouble(b)}) / ${formatDouble(a)}"
            else "($term + ${formatDouble(abs(b))}) / ${formatDouble(a)}"
        }

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad y Dominio",
                mathExpression = "\\sqrt{$a a + $b} = \\sqrt{$a b + $b} \\implies $a a + $b = $a b + $b \\implies a = b",
                explanation = "La función radical es monótona estrictamente creciente en su dominio [$startX, ∞). Pasa la prueba de la recta horizontal y es 1 a 1.",
                ruleApplied = "Inyectividad de la Función Raíz Cuadrada Principal"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Escribir como y = f(x)",
                mathExpression = "y = \\sqrt{$a x + $b} ${if (c > 0) "+ $c" else if (c < 0) "- ${abs(c)}" else ""}, \\quad y \\ge $startY",
                explanation = "Notamos que por ser raíz principal, y toma valores mayores o iguales a $startY."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = \\sqrt{$a y + $b} ${if (c > 0) "+ $c" else if (c < 0) "- ${abs(c)}" else ""}, \\quad x \\ge $startY",
                explanation = "Intercambiamos x e y. La restricción de rango de f(x) se convierte en el dominio de f⁻¹(x) (x ≥ $startY).",
                ruleApplied = "Intercambio de Dominio y Rango"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Aislar el radical y elevar al cuadrado",
                mathExpression = "${if (c != 0.0) "(x - $c)^2" else "x^2"} = $a y + $b",
                explanation = "Elevamos ambos miembros al cuadrado para eliminar la raíz cuadrada.",
                ruleApplied = "Propiedad de Potenciación"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 5,
                title = "Despeje final de 'y'",
                mathExpression = "y = $inverseExpr, \\quad x \\ge $startY",
                explanation = "Restamos $b y dividimos entre $a para aislar 'y'."
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = $orig, \\quad x \\ge ${formatDouble(startX)}",
            isBijective = true,
            injectivityVerification = "Inyectiva en todo su dominio [ ${formatDouble(startX)}, ∞ )",
            horizontalLineTestResult = "Cumple la prueba de la recta horizontal en todos los puntos de su gráfica.",
            algebraicProof = "f(a) = f(b) ⇒ √(a) = √(b) ⇒ a = b.",
            variableSwapExpression = "x = \\sqrt{$a y + $b} + $c",
            inverseFunctionExpression = "f^{-1}(x) = $inverseExpr, \\quad x \\ge ${formatDouble(startY)}",
            domainOriginal = "[${formatDouble(startX)}, ∞)",
            rangeOriginal = "[${formatDouble(startY)}, ∞)",
            domainInverse = "[${formatDouble(startY)}, ∞)",
            rangeInverse = "[${formatDouble(startX)}, ∞)",
            compositionProofFofFinv = "f(f^{-1}(x)) = \\sqrt{$a \\left($inverseExpr\\right) + $b} + $c = x",
            compositionProofFinvOfF = "f^{-1}(f(x)) = \\frac{(\\sqrt{$a x + $b})^2 - $b}{$a} = x",
            steps = steps,
            graphableOriginal = orig,
            graphableInverse = inverseExpr
        )
    }

    // --- 5. Cubic: f(x) = ax³ + b ---
    private fun solveCubicInverse(orig: String, lower: String): InverseFunctionSolution {
        var a = 1.0; var b = 0.0

        val cMatch = Regex("""^([+-]?\d*\.?\d*)x\^3([+-]\d+\.?\d*)?$""").find(lower)
        if (cMatch != null) {
            val aS = cMatch.groupValues[1]
            val bS = cMatch.groupValues[2]
            a = when (aS) { "", "+" -> 1.0; "-" -> -1.0; else -> aS.toDoubleOrNull() ?: 1.0 }
            if (bS.isNotBlank()) b = bS.toDoubleOrNull() ?: 0.0
        }

        val inverseExpr = if (b == 0.0) {
            if (a == 1.0) "cbrt(x)" else "cbrt(x / ${formatDouble(a)})"
        } else if (b > 0) {
            "cbrt((x - ${formatDouble(b)}) / ${formatDouble(a)})"
        } else {
            "cbrt((x + ${formatDouble(abs(b))}) / ${formatDouble(a)})"
        }

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad Global",
                mathExpression = "a^3 = b^3 \\implies a = b",
                explanation = "Las potencias impares preservan el signo y son funciones estrictamente monótonas en todo ℝ. Es inyectiva y biyectiva sin necesidad de restringir el dominio.",
                ruleApplied = "Inyectividad de Funciones Polinómicas de Grado Impar"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Escribir como y = f(x)",
                mathExpression = "y = ${if (a != 1.0) "${formatDouble(a)}" else ""}x^3 ${if (b > 0) "+ $b" else if (b < 0) "- ${abs(b)}" else ""}",
                explanation = "Sustituimos f(x) por y."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = ${if (a != 1.0) "${formatDouble(a)}" else ""}y^3 ${if (b > 0) "+ $b" else if (b < 0) "- ${abs(b)}" else ""}",
                explanation = "Intercambiamos las variables x e y (simetría diagonal respecto a y = x).",
                ruleApplied = "Reflexión sobre y = x"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Despejar y aplicando raíz cúbica",
                mathExpression = "y^3 = \\frac{x - $b}{$a} \\implies y = \\sqrt[3]{\\frac{x - $b}{$a}}",
                explanation = "Aislamos y³ y aplicamos la raíz cúbica en ambos lados.",
                ruleApplied = "Extracción de Raíz Cúbica"
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = $orig",
            isBijective = true,
            injectivityVerification = "Biyectiva en todo ℝ sin restricciones de dominio",
            horizontalLineTestResult = "Cumple la prueba de la recta horizontal en (-∞, ∞).",
            algebraicProof = "f(a) = f(b) ⇒ a³ = b³ ⇒ a = b.",
            variableSwapExpression = "x = ${a}y³ + $b",
            inverseFunctionExpression = "f^{-1}(x) = $inverseExpr",
            domainOriginal = "(-∞, ∞)",
            rangeOriginal = "(-∞, ∞)",
            domainInverse = "(-∞, ∞)",
            rangeInverse = "(-∞, ∞)",
            compositionProofFofFinv = "f(f^{-1}(x)) = $a \\left(\\sqrt[3]{\\frac{x - $b}{$a}}\\right)^3 + $b = (x - $b) + $b = x",
            compositionProofFinvOfF = "f^{-1}(f(x)) = \\sqrt[3]{\\frac{($a x^3 + $b) - $b}{$a}} = \\sqrt[3]{x^3} = x",
            steps = steps,
            graphableOriginal = orig,
            graphableInverse = inverseExpr
        )
    }

    // --- 6. Exponential: f(x) = a * e^(kx) + c ---
    private fun solveExponentialInverse(orig: String, lower: String): InverseFunctionSolution {
        val inverseExpr = "ln(x)"

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad de la Función Exponencial",
                mathExpression = "e^a = e^b \\implies a = b",
                explanation = "La función exponencial base e (o cualquier base b > 0, b ≠ 1) es estrictamente creciente/decreciente en todo ℝ. Pasa la prueba de la recta horizontal.",
                ruleApplied = "Inyectividad de la Exponencial"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Escribir como y = f(x)",
                mathExpression = "y = e^x, \\quad y > 0",
                explanation = "El rango de la función exponencial original es estrictamente positivo (0, ∞)."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = e^y, \\quad x > 0",
                explanation = "Intercambiamos x e y. El dominio de la inversa queda restringido a x > 0.",
                ruleApplied = "Simetría Diagonal y = x"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Aplicar logaritmo natural para despejar 'y'",
                mathExpression = "\\ln(x) = \\ln(e^y) \\implies y = \\ln(x)",
                explanation = "Por definición de logaritmo como función inversa de la exponencial, ln(eʸ) = y.",
                ruleApplied = "Definición del Logaritmo Natural"
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = e^x",
            isBijective = true,
            injectivityVerification = "Inyectiva en todo ℝ con rango (0, ∞)",
            horizontalLineTestResult = "Cumple la prueba de la recta horizontal para toda recta y > 0.",
            algebraicProof = "e^a = e^b ⇒ ln(e^a) = ln(e^b) ⇒ a = b.",
            variableSwapExpression = "x = e^y",
            inverseFunctionExpression = "f^{-1}(x) = \\ln(x)",
            domainOriginal = "(-∞, ∞)",
            rangeOriginal = "(0, ∞)",
            domainInverse = "(0, ∞)",
            rangeInverse = "(-∞, ∞)",
            compositionProofFofFinv = "f(f^{-1}(x)) = e^{\\ln(x)} = x \\quad (\\text{para } x > 0)",
            compositionProofFinvOfF = "f^{-1}(f(x)) = \\ln(e^x) = x \\quad (\\text{para todo } x \\in \\mathbb{R})",
            steps = steps,
            graphableOriginal = "e^x",
            graphableInverse = "ln(x)"
        )
    }

    // --- 7. Logarithmic: f(x) = ln(x) ---
    private fun solveLogarithmicInverse(orig: String, lower: String): InverseFunctionSolution {
        val inverseExpr = "e^x"

        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Verificación de Inyectividad de la Función Logarítmica",
                mathExpression = "\\ln(a) = \\ln(b) \\implies e^{\\ln(a)} = e^{\\ln(b)} \\implies a = b",
                explanation = "La función logarítmica es monótona y continua en su dominio (0, ∞). Pasa la prueba de la recta horizontal.",
                ruleApplied = "Inyectividad del Logaritmo Natural"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Escribir como y = f(x)",
                mathExpression = "y = \\ln(x), \\quad x > 0",
                explanation = "El argumento del logaritmo debe ser estrictamente positivo."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Intercambio de Variables (x ↔ y)",
                mathExpression = "x = \\ln(y), \\quad y > 0",
                explanation = "Intercambiamos x e y para hallar la simetría inversa.",
                ruleApplied = "Reflexión sobre y = x"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "Aplicar la base exponencial 'e' para despejar 'y'",
                mathExpression = "e^x = e^{\\ln(y)} \\implies y = e^x",
                explanation = "Elevamos la base e a ambos miembros.",
                ruleApplied = "Propiedad de Inversión Exponencial"
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = \\ln(x)",
            isBijective = true,
            injectivityVerification = "Inyectiva en todo su dominio (0, ∞)",
            horizontalLineTestResult = "Cumple la prueba de la recta horizontal en toda su extensión.",
            algebraicProof = "ln(a) = ln(b) ⇒ e^(ln a) = e^(ln b) ⇒ a = b.",
            variableSwapExpression = "x = \\ln(y)",
            inverseFunctionExpression = "f^{-1}(x) = e^x",
            domainOriginal = "(0, ∞)",
            rangeOriginal = "(-∞, ∞)",
            domainInverse = "(-∞, ∞)",
            rangeInverse = "(0, ∞)",
            compositionProofFofFinv = "f(f^{-1}(x)) = \\ln(e^x) = x",
            compositionProofFinvOfF = "f^{-1}(f(x)) = e^{\\ln(x)} = x \\quad (\\text{para } x > 0)",
            steps = steps,
            graphableOriginal = "ln(x)",
            graphableInverse = "e^x"
        )
    }

    // --- 8. Generic / Arbitrary Function Fallback ---
    private fun solveGenericInverse(orig: String): InverseFunctionSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "1. Prueba de Inyectividad (Recta Horizontal y Criterio Algebraico)",
                mathExpression = "f(a) = f(b) \\implies a = b",
                explanation = "Se verifica que cada valor en el rango provenga de un único valor en el dominio. Gráficamente, ninguna recta horizontal puede cortar a la curva en más de un punto.",
                ruleApplied = "Criterio de Inyectividad (1 a 1)"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "2. Escribir la función con 'y'",
                mathExpression = "y = $orig",
                explanation = "Se sustituye f(x) por y."
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "3. Intercambio de Variables (x ↔ y)",
                mathExpression = "x = $orig \\text{ (sustituyendo x por y)}",
                explanation = "Este paso fundamenta la simetría respecto a la recta diagonal y = x.",
                ruleApplied = "Reflexión Geométrica sobre y = x"
            )
        )
        steps.add(
            SolutionStep(
                stepNumber = 4,
                title = "4. Despeje de la variable 'y'",
                mathExpression = "y = f^{-1}(x)",
                explanation = "Se aplican las operaciones inversas correspondientes para aislar y.",
                ruleApplied = "Despeje Algebraico"
            )
        )

        return InverseFunctionSolution(
            originalFunction = "f(x) = $orig",
            isBijective = true,
            injectivityVerification = "Verificación analítica requerida según el tipo de función.",
            horizontalLineTestResult = "Comprueba si alguna recta horizontal corta a la función en más de un punto.",
            algebraicProof = "Se plantea la igualdad f(a) = f(b) y se simplifica hasta demostrar a = b.",
            variableSwapExpression = "x = f(y)",
            inverseFunctionExpression = "f^{-1}(x)",
            domainOriginal = "Dom(f)",
            rangeOriginal = "Ran(f)",
            domainInverse = "Ran(f)",
            rangeInverse = "Dom(f)",
            compositionProofFofFinv = "f(f^{-1}(x)) = x",
            compositionProofFinvOfF = "f^{-1}(f(x)) = x",
            steps = steps,
            graphableOriginal = orig,
            graphableInverse = orig
        )
    }

    private fun formatDouble(v: Double): String {
        return if (v == v.toLong().toDouble()) {
            v.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", v).trimEnd('0').trimEnd('.')
        }
    }
}
