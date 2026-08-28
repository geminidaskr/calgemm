package com.example.math

/**
 * Mathematical models for Precalculus functions, graphs, equations, and step-by-step solver.
 */

data class FunctionDefinition(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "f(x)",
    val expression: String = "x^2 - 4",
    val colorHex: Long = 0xFF88C0D0, // Nord8
    val isVisible: Boolean = true
)

data class GraphViewport(
    val minX: Float = -10f,
    val maxX: Float = 10f,
    val minY: Float = -10f,
    val maxY: Float = 10f
) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
    val centerX: Float get() = (minX + maxX) / 2f
    val centerY: Float get() = (minY + maxY) / 2f
}

data class Point2D(
    val x: Double,
    val y: Double,
    val label: String? = null,
    val pointType: PointType = PointType.REGULAR
)

enum class PointType {
    REGULAR,
    ROOT, // Cero / Raíz
    Y_INTERCEPT, // Intercepto en Y
    LOCAL_MAX, // Máximo local
    LOCAL_MIN, // Mínimo local
    DISCONTINUITY // Punto de discontinuidad
}

data class Asymptote(
    val isVertical: Boolean,
    val value: Double,
    val equationText: String
)

data class FunctionAnalysis(
    val expression: String,
    val functionType: String,
    val domain: String,
    val range: String,
    val roots: List<Double>,
    val yIntercept: Double?,
    val symmetry: String, // Par, Impar, Ninguna
    val verticalAsymptotes: List<Double>,
    val horizontalAsymptotes: List<Double>,
    val localExtrema: List<Point2D>,
    val derivativePreview: String,
    val behaviorAtInfinity: String,
    val isContinuous: Boolean
)

data class SolutionStep(
    val stepNumber: Int,
    val title: String,
    val mathExpression: String,
    val explanation: String,
    val ruleApplied: String? = null
)

data class EquationSolution(
    val originalEquation: String,
    val equationType: String,
    val solutions: List<String>,
    val steps: List<SolutionStep>,
    val notes: String? = null
)

data class InverseFunctionSolution(
    val originalFunction: String,
    val isBijective: Boolean,
    val injectivityVerification: String,
    val horizontalLineTestResult: String,
    val algebraicProof: String,
    val variableSwapExpression: String,
    val inverseFunctionExpression: String,
    val domainOriginal: String,
    val rangeOriginal: String,
    val domainInverse: String,
    val rangeInverse: String,
    val compositionProofFofFinv: String,
    val compositionProofFinvOfF: String,
    val steps: List<SolutionStep>,
    val domainRestrictionNote: String? = null,
    val graphableOriginal: String,
    val graphableInverse: String
)

data class PrecalcTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: TopicCategory,
    val iconName: String,
    val summary: String,
    val theoryMarkdown: String,
    val keyFormulas: List<KeyFormula>,
    val examples: List<TopicExample>
)

enum class TopicCategory {
    FUNCTIONS_AND_GRAPHS,
    POLYNOMIAL_AND_RATIONAL,
    EXPONENTIAL_AND_LOGARITHMIC,
    TRIGONOMETRY,
    TRANSFORMATIONS_AND_COMPOSITION,
    EQUATIONS_AND_INEQUALITIES,
    STATISTICS_AND_PROBABILITY
}

data class KeyFormula(
    val name: String,
    val formula: String,
    val description: String
)

data class TopicExample(
    val title: String,
    val problem: String,
    val solutionSteps: List<SolutionStep>,
    val finalAnswer: String,
    val graphableFunction: String? = null
)

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val expression: String,
    val type: String, // FUNCTION, EQUATION
    val timestamp: Long = System.currentTimeMillis(),
    val resultPreview: String
)
