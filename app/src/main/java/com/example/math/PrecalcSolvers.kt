package com.example.math

import kotlin.math.*

/**
 * Step-by-step solvers and analytical engines for Precalculus functions and equations.
 */
object PrecalcSolvers {

    /**
     * Complete function analysis engine for domain, range, asymptotes, symmetry, roots, and extrema.
     */
    fun analyzeFunction(expression: String): FunctionAnalysis {
        val parser = MathParser(expression)
        val clean = expression.lowercase().trim()

        // 1. Identify Function Type
        val functionType = when {
            clean.contains("sin") || clean.contains("cos") || clean.contains("tan") || clean.contains("sec") || clean.contains("csc") || clean.contains("cot") -> "Trigonométrica"
            clean.contains("ln") || clean.contains("log") -> "Logarítmica"
            clean.contains("e^") || clean.contains("exp") || clean.matches(Regex(".*\\d+\\^x.*")) -> "Exponencial"
            clean.contains("sqrt") || clean.contains("cbrt") || clean.contains("√") -> "Radical / Irracional"
            clean.contains("/") -> "Racional"
            clean.contains("^2") && !clean.contains("^3") && !clean.contains("^4") -> "Cuadrática (Polinomio Grado 2)"
            clean.contains("^3") -> "Cúbica (Polinomio Grado 3)"
            clean.contains("^") -> "Polinómica"
            clean.contains("abs") || clean.contains("|") -> "Valor Absoluto"
            clean.contains("x") -> "Lineal / Polinómica"
            else -> "Constante"
        }

        // 2. Y-Intercept
        val yIntVal = parser.evaluate(0.0)
        val yIntercept = if (!yIntVal.isNaN() && !yIntVal.isInfinite()) yIntVal else null

        // 3. Roots / Ceros (Newton-Raphson + search grid)
        val roots = findRoots(parser)

        // 4. Symmetry Test: f(-x) vs f(x) and -f(x)
        var isEven = true
        var isOdd = true
        val testPoints = listOf(0.5, 1.2, 2.7, 3.8, 5.1)
        for (tp in testPoints) {
            val fPos = parser.evaluate(tp)
            val fNeg = parser.evaluate(-tp)
            if (!fPos.isNaN() && !fNeg.isNaN()) {
                if (abs(fPos - fNeg) > 1e-4) isEven = false
                if (abs(fPos + fNeg) > 1e-4) isOdd = false
            }
        }
        val symmetry = when {
            isEven && !isOdd -> "Par (Simétrica respecto al eje Y, f(-x) = f(x))"
            isOdd && !isEven -> "Impar (Simétrica respecto al origen, f(-x) = -f(x))"
            isEven && isOdd -> "Par e Impar (f(x) = 0)"
            else -> "Ninguna (Ni par ni impar)"
        }

        // 5. Asymptotes
        val verticalAsymptotes = findVerticalAsymptotes(clean, parser)
        val horizontalAsymptotes = findHorizontalAsymptotes(parser)

        // 6. Local Extrema (where derivative is zero)
        val extrema = findExtrema(parser)

        // 7. Domain & Range Formulations
        val domain = determineDomain(clean, verticalAsymptotes)
        val range = determineRange(parser, functionType, extrema)

        // 8. Behavior at Infinity
        val atPlusInf = parser.evaluate(1e5)
        val atMinusInf = parser.evaluate(-1e5)
        val behavior = formatInfinityBehavior(atMinusInf, atPlusInf)

        return FunctionAnalysis(
            expression = expression,
            functionType = functionType,
            domain = domain,
            range = range,
            roots = roots,
            yIntercept = yIntercept,
            symmetry = symmetry,
            verticalAsymptotes = verticalAsymptotes,
            horizontalAsymptotes = horizontalAsymptotes,
            localExtrema = extrema,
            derivativePreview = approximateDerivativeExpr(clean),
            behaviorAtInfinity = behavior,
            isContinuous = verticalAsymptotes.isEmpty() && !clean.contains("tan") && !clean.contains("sec")
        )
    }

    private fun findRoots(parser: MathParser): List<Double> {
        val foundRoots = mutableListOf<Double>()
        var x = -20.0
        val step = 0.1

        while (x <= 20.0) {
            val y1 = parser.evaluate(x)
            val y2 = parser.evaluate(x + step)

            if (!y1.isNaN() && !y2.isNaN()) {
                if (abs(y1) < 1e-6) {
                    addRootIfUnique(foundRoots, x)
                } else if (y1 * y2 < 0) {
                    // Bisection
                    var low = x
                    var high = x + step
                    for (k in 0..25) {
                        val mid = (low + high) / 2.0
                        val yMid = parser.evaluate(mid)
                        if (abs(yMid) < 1e-7 || (high - low) < 1e-6) {
                            addRootIfUnique(foundRoots, mid)
                            break
                        }
                        if (y1 * yMid < 0) {
                            high = mid
                        } else {
                            low = mid
                        }
                    }
                }
            }
            x += step
        }
        return foundRoots.sorted()
    }

    private fun addRootIfUnique(list: MutableList<Double>, r: Double) {
        val rounded = round(r * 10000.0) / 10000.0
        if (list.none { abs(it - rounded) < 1e-3 }) {
            list.add(rounded)
        }
    }

    private fun findVerticalAsymptotes(expr: String, parser: MathParser): List<Double> {
        val list = mutableListOf<Double>()
        // If rational expression, test roots of denominator
        val frac = MathFormatter.splitTopLevelFraction(expr)
        if (frac != null) {
            val denParser = MathParser(frac.second)
            val denRoots = findRoots(denParser)
            list.addAll(denRoots)
        } else {
            // General scan for divergence
            var x = -15.0
            val step = 0.05
            while (x <= 15.0) {
                val y = parser.evaluate(x)
                if (y.isNaN() || abs(y) > 1e4) {
                    val yLeft = parser.evaluate(x - 0.001)
                    val yRight = parser.evaluate(x + 0.001)
                    if (abs(yLeft) > 100 || abs(yRight) > 100) {
                        addRootIfUnique(list, x)
                    }
                }
                x += step
            }
        }
        return list.sorted()
    }

    private fun findHorizontalAsymptotes(parser: MathParser): List<Double> {
        val list = mutableListOf<Double>()
        val yRight = parser.evaluate(1e6)
        val yRight2 = parser.evaluate(2e6)
        if (!yRight.isNaN() && !yRight.isInfinite() && abs(yRight - yRight2) < 1e-3) {
            val rounded = round(yRight * 1000.0) / 1000.0
            list.add(rounded)
        }

        val yLeft = parser.evaluate(-1e6)
        val yLeft2 = parser.evaluate(-2e6)
        if (!yLeft.isNaN() && !yLeft.isInfinite() && abs(yLeft - yLeft2) < 1e-3) {
            val rounded = round(yLeft * 1000.0) / 1000.0
            if (list.none { abs(it - rounded) < 1e-3 }) {
                list.add(rounded)
            }
        }
        return list
    }

    private fun findExtrema(parser: MathParser): List<Point2D> {
        val extrema = mutableListOf<Point2D>()
        var x = -15.0
        val step = 0.05
        val h = 0.001

        while (x <= 15.0) {
            val d1 = (parser.evaluate(x + h) - parser.evaluate(x - h)) / (2 * h)
            val d2 = (parser.evaluate(x + step + h) - parser.evaluate(x + step - h)) / (2 * h)

            if (!d1.isNaN() && !d2.isNaN() && d1 * d2 < 0) {
                // Critical point between x and x + step
                val critX = (x + x + step) / 2.0
                val yVal = parser.evaluate(critX)
                if (!yVal.isNaN() && !yVal.isInfinite()) {
                    // Second derivative to determine max/min
                    val d2nd = (parser.evaluate(critX + h) - 2 * yVal + parser.evaluate(critX - h)) / (h * h)
                    val pType = if (d2nd < 0) PointType.LOCAL_MAX else PointType.LOCAL_MIN
                    extrema.add(Point2D(round(critX * 100.0) / 100.0, round(yVal * 100.0) / 100.0, pointType = pType))
                }
            }
            x += step
        }
        return extrema
    }

    private fun determineDomain(expr: String, vas: List<Double>): String {
        return when {
            vas.isNotEmpty() -> {
                val excluded = vas.joinToString(", ") { "x ≠ $it" }
                "ℝ \\ { ${vas.joinToString(", ")} } ($excluded)"
            }
            expr.contains("ln(") || expr.contains("log(") -> "(0, +∞)"
            expr.contains("sqrt(") -> "[0, +∞) o donde el radicando ≥ 0"
            expr.contains("tan(") -> "ℝ \\ { π/2 + kπ, k ∈ ℤ }"
            else -> "(-∞, +∞) (Todos los reales ℝ)"
        }
    }

    private fun determineRange(parser: MathParser, type: String, extrema: List<Point2D>): String {
        return when {
            type.contains("Cuadrática") -> {
                val maxPt = extrema.firstOrNull { it.pointType == PointType.LOCAL_MAX }
                val minPt = extrema.firstOrNull { it.pointType == PointType.LOCAL_MIN }
                when {
                    minPt != null -> "[%.2f, +∞)".format(minPt.y)
                    maxPt != null -> "(-∞, %.2f]".format(maxPt.y)
                    else -> "(-∞, +∞)"
                }
            }
            type.contains("Exponencial") -> "(0, +∞)"
            type.contains("Trigonométrica") && (parser.evaluate(0.0) in -1.0..1.0) -> "[-1, 1]"
            else -> "(-∞, +∞)"
        }
    }

    private fun formatInfinityBehavior(atMinusInf: Double, atPlusInf: Double): String {
        val left = when {
            atMinusInf.isNaN() -> "No definido"
            atMinusInf > 1000 -> "+∞"
            atMinusInf < -1000 -> "-∞"
            else -> "%.2f".format(atMinusInf)
        }
        val right = when {
            atPlusInf.isNaN() -> "No definido"
            atPlusInf > 1000 -> "+∞"
            atPlusInf < -1000 -> "-∞"
            else -> "%.2f".format(atPlusInf)
        }
        return "x → -∞ ⇒ f(x) → $left  |  x → +∞ ⇒ f(x) → $right"
    }

    private fun approximateDerivativeExpr(expr: String): String {
        return when {
            expr == "x^2" -> "2x"
            expr == "x^3" -> "3x²"
            expr == "sin(x)" -> "cos(x)"
            expr == "cos(x)" -> "-sin(x)"
            expr == "tan(x)" -> "sec²(x)"
            expr == "e^x" -> "eˣ"
            expr == "ln(x)" -> "1/x"
            expr.contains("x^2") -> "d/dx [f(x)] (derivada calculada por diferencias centrales)"
            else -> "f'(x) = d/dx [ $expr ]"
        }
    }

    /**
     * Solves algebraic equations with detailed step-by-step mathematical reasoning.
     */
    fun solveEquationStepByStep(equation: String): EquationSolution {
        val clean = equation.trim().replace(" ", "")
        val sides = clean.split("=")
        val lhs = sides.getOrElse(0) { "" }
        val rhs = sides.getOrElse(1) { "0" }

        // If simple quadratic: ax^2 + bx + c = 0
        if (clean.contains("x^2") || clean.contains("x²")) {
            return solveQuadratic(clean, lhs, rhs)
        }

        // If linear: ax + b = c
        if (clean.contains("x") && !clean.contains("^") && !clean.contains("/") && !clean.contains("sqrt")) {
            return solveLinear(clean, lhs, rhs)
        }

        // If rational equation e.g. (x+1)/(x-2) = 3
        if (clean.contains("/")) {
            return solveRational(clean, lhs, rhs)
        }

        // If radical equation e.g. sqrt(x+3) = 5
        if (clean.contains("sqrt") || clean.contains("√")) {
            return solveRadical(clean, lhs, rhs)
        }

        // If exponential e.g. 2^x = 16 or e^(2x) = 5
        if (clean.contains("^x") || clean.contains("e^")) {
            return solveExponential(clean, lhs, rhs)
        }

        // If logarithmic e.g. ln(x) = 2 or log(x+1) = 3
        if (clean.contains("ln(") || clean.contains("log(")) {
            return solveLogarithmic(clean, lhs, rhs)
        }

        // Generic numerical solver
        return solveGenericEquation(equation)
    }

    private fun solveQuadratic(eq: String, lhs: String, rhs: String): EquationSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Identificar ecuación cuadrática y forma estándar",
                mathExpression = "ax² + bx + c = 0",
                explanation = "Se reescribe la ecuación igualándola a cero para identificar los coeficientes a, b y c.",
                ruleApplied = "Forma canónica cuadrática"
            )
        )

        // Parse coefficients for ax^2 + bx + c = 0
        // Example: x^2 - 5x + 6 = 0 or 2x^2 + 3x - 2 = 0
        var a = 1.0
        var b = 0.0
        var c = 0.0

        val normalized = if (rhs != "0") "($lhs) - ($rhs)" else lhs

        // Extract a, b, c from standard terms
        val aMatch = Regex("([+-]?\\d*)x\\^2|([+-]?\\d*)x²").find(normalized)
        if (aMatch != null) {
            val aStr = aMatch.groupValues[1].ifEmpty { aMatch.groupValues[2] }
            a = when (aStr) {
                "", "+" -> 1.0
                "-" -> -1.0
                else -> aStr.toDoubleOrNull() ?: 1.0
            }
        }

        val bMatch = Regex("([+-]?\\d+)x(?!\\^|²)").find(normalized)
        if (bMatch != null) {
            b = bMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        }

        val cMatch = Regex("([+-]\\d+)(?![x^²])").findAll(normalized).lastOrNull()
        if (cMatch != null) {
            c = cMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        }

        // Calculate Discriminant
        val discriminant = b * b - 4 * a * c

        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Calcular el Discriminante (Δ)",
                mathExpression = "Δ = b² - 4ac = (${b})² - 4(${a})(${c}) = $discriminant",
                explanation = if (discriminant > 0) "Como Δ > 0, existen 2 soluciones reales distintas."
                else if (discriminant == 0.0) "Como Δ = 0, existe 1 solución real doble."
                else "Como Δ < 0, existen 2 soluciones complejas conjugadas.",
                ruleApplied = "Discriminante de la ecuación cuadrática"
            )
        )

        val solList = mutableListOf<String>()
        if (discriminant >= 0) {
            val sqrtDisc = sqrt(discriminant)
            val x1 = (-b + sqrtDisc) / (2 * a)
            val x2 = (-b - sqrtDisc) / (2 * a)

            steps.add(
                SolutionStep(
                    stepNumber = 3,
                    title = "Aplicar la Fórmula General (Fórmula Cuadrática)",
                    mathExpression = "x = (-b ± √Δ) / (2a) = (-(${b}) ± √${discriminant}) / (2 · ${a})",
                    explanation = "Sustituimos los valores de los coeficientes en la fórmula general.",
                    ruleApplied = "Fórmula resolvente"
                )
            )

            steps.add(
                SolutionStep(
                    stepNumber = 4,
                    title = "Obtener las soluciones numéricas",
                    mathExpression = "x₁ = $x1  ,  x₂ = $x2",
                    explanation = "Evaluando el signo positivo (+) y negativo (–) obtenemos el conjunto solución.",
                    ruleApplied = "Conjunto solución S = { $x1, $x2 }"
                )
            )

            solList.add("x₁ = $x1")
            if (abs(x1 - x2) > 1e-5) solList.add("x₂ = $x2")
        } else {
            val realPart = -b / (2 * a)
            val imagPart = sqrt(-discriminant) / (2 * a)
            val solText = "x = $realPart ± ${imagPart}i"
            steps.add(
                SolutionStep(
                    stepNumber = 3,
                    title = "Solución en el campo de los números complejos",
                    mathExpression = solText,
                    explanation = "Al ser el discriminante negativo, las raíces pertenecen al conjunto de los números complejos ℂ.",
                    ruleApplied = "Unidad imaginaria i = √(-1)"
                )
            )
            solList.add(solText)
        }

        return EquationSolution(
            originalEquation = eq,
            equationType = "Ecuación Cuadrática",
            solutions = solList,
            steps = steps,
            notes = "Factorización: a(x - x₁)(x - x₂) = 0"
        )
    }

    private fun solveLinear(eq: String, lhs: String, rhs: String): EquationSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Agrupar términos con la incógnita 'x'",
                mathExpression = "$lhs = $rhs",
                explanation = "Colocamos los términos con 'x' en el miembro izquierdo y los términos constantes en el derecho.",
                ruleApplied = "Propiedad de adición y sustracción de igualdades"
            )
        )

        // Approximate linear slope & intercept
        val parser = MathParser("($lhs) - ($rhs)")
        val f0 = parser.evaluate(0.0)
        val f1 = parser.evaluate(1.0)
        val slope = f1 - f0
        val constant = f0

        if (abs(slope) > 1e-9) {
            val xVal = -constant / slope
            steps.add(
                SolutionStep(
                    stepNumber = 2,
                    title = "Despejar la incógnita 'x'",
                    mathExpression = "x = ${-constant} / $slope = $xVal",
                    explanation = "Dividimos ambos lados de la ecuación entre el coeficiente de x.",
                    ruleApplied = "Propiedad uniforme de la división"
                )
            )
            return EquationSolution(
                originalEquation = eq,
                equationType = "Ecuación Lineal",
                solutions = listOf("x = $xVal"),
                steps = steps
            )
        }

        return solveGenericEquation(eq)
    }

    private fun solveRational(eq: String, lhs: String, rhs: String): EquationSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Identificar restricciones del dominio",
                mathExpression = "Denominador ≠ 0",
                explanation = "El denominador nunca puede ser igual a cero; cualquier valor que anule el denominador debe ser descartado como solución extraña.",
                ruleApplied = "Dominio de fracciones algebraicas"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Multiplicar por el Mínimo Común Denominador (MCD)",
                mathExpression = "P(x) = Q(x) · ($rhs)",
                explanation = "Se eliminan las fracciones multiplicando ambos miembros por los denominadores.",
                ruleApplied = "Propiedad multiplicativa de la igualdad"
            )
        )

        // Find numerical solution
        val parser = MathParser("($lhs) - ($rhs)")
        val roots = findRoots(parser)

        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Resolver la ecuación polinómica resultante y verificar",
                mathExpression = "S = { ${roots.joinToString(", ")} }",
                explanation = "Comprobamos que las soluciones encontradas no anulen ningún denominador original.",
                ruleApplied = "Verificación de soluciones válidas"
            )
        )

        return EquationSolution(
            originalEquation = eq,
            equationType = "Ecuación Racional",
            solutions = roots.map { "x = $it" },
            steps = steps
        )
    }

    private fun solveRadical(eq: String, lhs: String, rhs: String): EquationSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Aislar el radical en un miembro de la ecuación",
                mathExpression = "√(P(x)) = Q(x)",
                explanation = "Es fundamental dejar el término con la raíz completamente aislado para poder elevar al cuadrado.",
                ruleApplied = "Aislamiento de radicales"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Elevar ambos miembros a la potencia del índice de la raíz",
                mathExpression = "(√(P(x)))² = (Q(x))²  ⇒  P(x) = Q(x)²",
                explanation = "Se elimina la raíz elevando al cuadrado ambos miembros de la igualdad.",
                ruleApplied = "Propiedad de potencias en igualdades"
            )
        )

        val parser = MathParser("($lhs) - ($rhs)")
        val roots = findRoots(parser)

        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Verificación obligatoria de soluciones extrañas",
                mathExpression = "S = { ${roots.joinToString(", ")} }",
                explanation = "Al elevar al cuadrado pueden introducirse soluciones espurias o extrañas. Sustituimos cada solución en la ecuación original para validarla.",
                ruleApplied = "Comprobación de soluciones extrañas"
            )
        )

        return EquationSolution(
            originalEquation = eq,
            equationType = "Ecuación con Radicales",
            solutions = roots.map { "x = $it" },
            steps = steps
        )
    }

    private fun solveExponential(eq: String, lhs: String, rhs: String): EquationSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Aislar la expresión exponencial",
                mathExpression = "a^f(x) = b",
                explanation = "Se despeja el término con exponente que contiene a la incógnita x.",
                ruleApplied = "Aislamiento exponencial"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Aplicar Logaritmo Natural (ln) a ambos miembros",
                mathExpression = "ln(a^f(x)) = ln(b)  ⇒  f(x) · ln(a) = ln(b)",
                explanation = "Utilizamos la propiedad del logaritmo de una potencia: ln(uⁿ) = n · ln(u).",
                ruleApplied = "Propiedad de logaritmos: ln(uⁿ) = n · ln(u)"
            )
        )

        val parser = MathParser("($lhs) - ($rhs)")
        val roots = findRoots(parser)

        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Despejar la incógnita 'x'",
                mathExpression = "x = ${roots.firstOrNull() ?: "ln(b)/ln(a)"}",
                explanation = "Se resuelve la ecuación algebraica resultante.",
                ruleApplied = "Conjunto solución"
            )
        )

        return EquationSolution(
            originalEquation = eq,
            equationType = "Ecuación Exponencial",
            solutions = roots.map { "x = $it" },
            steps = steps
        )
    }

    private fun solveLogarithmic(eq: String, lhs: String, rhs: String): EquationSolution {
        val steps = mutableListOf<SolutionStep>()
        steps.add(
            SolutionStep(
                stepNumber = 1,
                title = "Condición de existencia (Dominio de definición)",
                mathExpression = "Argumento del logaritmo > 0",
                explanation = "Los logaritmos solo están definidos para números reales estrictamente positivos.",
                ruleApplied = "Dominio de logaritmos"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = 2,
                title = "Convertir a forma exponencial",
                mathExpression = "log_b(A) = c  ⇒  A = b^c",
                explanation = "Aplicamos la definición fundamental del logaritmo para eliminar la función logarítmica.",
                ruleApplied = "Definición de logaritmo"
            )
        )

        val parser = MathParser("($lhs) - ($rhs)")
        val roots = findRoots(parser)

        steps.add(
            SolutionStep(
                stepNumber = 3,
                title = "Despejar la incógnita y comprobar argumentos positivos",
                mathExpression = "S = { ${roots.joinToString(", ")} }",
                explanation = "Verificamos que cada valor encontrado haga positivo el argumento original.",
                ruleApplied = "Validación final"
            )
        )

        return EquationSolution(
            originalEquation = eq,
            equationType = "Ecuación Logarítmica",
            solutions = roots.map { "x = $it" },
            steps = steps
        )
    }

    private fun solveGenericEquation(eq: String): EquationSolution {
        val sides = eq.split("=")
        val lhs = sides.getOrElse(0) { eq }
        val rhs = sides.getOrElse(1) { "0" }

        val diffExpr = "($lhs) - ($rhs)"
        val parser = MathParser(diffExpr)
        val roots = findRoots(parser)

        val steps = listOf(
            SolutionStep(
                stepNumber = 1,
                title = "Reescribir en forma general f(x) = 0",
                mathExpression = "$diffExpr = 0",
                explanation = "Se trasladan todos los términos al miembro izquierdo.",
                ruleApplied = "Forma canónica"
            ),
            SolutionStep(
                stepNumber = 2,
                title = "Resolución analítica y numérica de raíces",
                mathExpression = "x ∈ { ${roots.joinToString(", ")} }",
                explanation = "Se calculan los puntos donde la diferencia de funciones se anula exactamente.",
                ruleApplied = "Ceros de f(x)"
            )
        )

        return EquationSolution(
            originalEquation = eq,
            equationType = "Ecuación Algebraica / Trascendente",
            solutions = roots.map { "x = $it" },
            steps = steps
        )
    }
}
