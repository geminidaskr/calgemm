package com.example.math

enum class StatDataType(val displayName: String) {
    NUMERICAL_UNIVARIATE("Numérica Continua / Discreta"),
    CATEGORICAL_FREQUENCY("Categórica / Cualitativa"),
    BIVARIATE_PAIRS("Bivariada (Pares X, Y)")
}

enum class RecommendedChartType(val displayName: String, val iconName: String) {
    BAR_CHART("Diagrama de Barras", "BarChart"),
    HISTOGRAM("Histograma", "ViewColumn"),
    FREQUENCY_POLYGON("Polígono de Frecuencias", "ShowChart"),
    PIE_CHART("Gráfico Circular (Pastel)", "PieChart"),
    SCATTER_PLOT("Diagrama de Dispersión", "BubbleChart"),
    BOX_PLOT("Diagrama de Caja y Bigotes", "Inbox")
}

data class ChartRecommendation(
    val recommendedType: RecommendedChartType,
    val confidence: Float,
    val title: String,
    val reason: String,
    val whyNotOthers: String,
    val alternativeTypes: List<RecommendedChartType>
)

data class FrequencyTableEntry(
    val index: Int,
    val label: String,             // "[10 - 20)" or "Categoría A"
    val lowerBound: Double,
    val upperBound: Double,
    val classMark: Double,         // xi
    val absoluteFrequency: Int,    // fi
    val cumulativeAbsolute: Int,   // Fi
    val relativeFrequency: Double, // hi
    val cumulativeRelative: Double,// Hi
    val percentage: Double,        // hi %
    val pieAngleDegrees: Double    // degrees for pie chart
)

data class FrequencyTable(
    val entries: List<FrequencyTableEntry>,
    val totalCount: Int,
    val numClasses: Int,
    val classWidth: Double,
    val minVal: Double,
    val maxVal: Double
)

data class DescriptiveStats(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val modes: List<Double>,
    val modeDescription: String,
    val min: Double,
    val max: Double,
    val range: Double,
    val sampleVariance: Double,
    val populationVariance: Double,
    val sampleStdDev: Double,
    val populationStdDev: Double,
    val meanAbsoluteDeviation: Double,
    val coefficientOfVariation: Double, // in %
    val geometricMean: Double?,
    val harmonicMean: Double?,
    val q1: Double,
    val q2: Double,
    val q3: Double,
    val iqr: Double,
    val lowerFence: Double,
    val upperFence: Double,
    val outliers: List<Double>,
    val skewness: Double,
    val kurtosis: Double
)

data class CategoricalItem(
    val label: String,
    val frequency: Double,
    val percentage: Double,
    val colorHex: Long
)

data class BivariatePoint(
    val x: Double,
    val y: Double,
    val label: String? = null
)

data class BivariateStats(
    val points: List<BivariatePoint>,
    val count: Int,
    val meanX: Double,
    val meanY: Double,
    val varianceX: Double,
    val varianceY: Double,
    val stdDevX: Double,
    val stdDevY: Double,
    val covariance: Double,
    val pearsonR: Double,
    val rSquared: Double,
    val slope: Double,              // m
    val intercept: Double,          // b (y = mx + b)
    val regressionEquation: String, // "y = 1.45x + 3.20"
    val correlationType: String,    // "Correlación Positiva Fuerte", etc.
    val correlationInterpretation: String
)

data class VarianceStepItem(
    val index: Int,
    val x: Double,
    val xStr: String,
    val deviation: Double,             // xi - mean
    val deviationCalcStr: String,      // "5 - 3.6 = +1.4"
    val deviationFormatted: String,    // "+1.4" or "-2.6"
    val squaredDeviation: Double,      // (xi - mean)^2
    val squaredCalcStr: String,        // "(1.4)² = 1.96"
    val squaredFormatted: String       // "1.96"
)

data class VarianceProcedure(
    val mean: Double,
    val meanFormatted: String,
    val count: Int,
    val items: List<VarianceStepItem>,
    val sumDeviations: Double,              // ~0.0
    val sumDeviationsFormatted: String,     // "0"
    val sumSquaredDeviations: Double,       // e.g. 23.2
    val sumSquaredDeviationsFormatted: String, // "23.2"
    // Population
    val populationVarianceFormula: String,     // "σ² = Σ(x - μ)² / N"
    val populationVarianceFraction: String,    // "23.2 / 5"
    val populationVarianceValue: Double,       // 4.64
    val populationVarianceFormatted: String,   // "4.64"
    val populationStdDevFormula: String,       // "σ = √4.64"
    val populationStdDevValue: Double,         // 2.15
    val populationStdDevFormatted: String,     // "2.15"
    // Sample
    val sampleVarianceFormula: String,         // "s² = Σ(x - x̄)² / (n - 1)"
    val sampleVarianceFraction: String,        // "23.2 / (5 - 1) = 23.2 / 4"
    val sampleVarianceValue: Double,           // 5.8
    val sampleVarianceFormatted: String,       // "5.8"
    val sampleStdDevFormula: String,           // "s = √5.8"
    val sampleStdDevValue: Double,             // 2.41
    val sampleStdDevFormatted: String          // "2.41"
)

data class StatisticsAnalysisResult(
    val rawInput: String,
    val dataType: StatDataType,
    val rawNumbers: List<Double>,
    val categoricalItems: List<CategoricalItem>,
    val bivariatePoints: List<BivariatePoint>,
    val descriptiveStats: DescriptiveStats?,
    val frequencyTable: FrequencyTable?,
    val bivariateStats: BivariateStats?,
    val varianceProcedure: VarianceProcedure?,
    val recommendation: ChartRecommendation
)
