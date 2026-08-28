package com.example.math

import java.util.Locale
import kotlin.math.*

object StatisticsCalculator {

    // Distinct palette colors for categorical and pie slices
    val PALETTE_COLORS = listOf(
        0xFF88C0D0, // Nord8 Frost Cyan
        0xFF81A1C1, // Nord9 Frost Arctic Blue
        0xFF5E81AC, // Nord10 Frost Alpine Blue
        0xFFA3BE8C, // Nord14 Aurora Green
        0xFFEBCB8B, // Nord13 Aurora Yellow
        0xFFD08770, // Nord12 Aurora Orange
        0xFFBF616A, // Nord11 Aurora Red
        0xFFB48EAD, // Nord15 Aurora Purple
        0xFF8FBCBB, // Nord7 Frost Teal
        0xFF4C566A  // Nord3 Polar Slate
    )

    /**
     * Parses any input text and returns full statistical analysis
     * Handles:
     * - Bivariate: (1, 2.5), (2, 4.0) or 1:2.5, 2:4.0 or x y pairs
     * - Categorical: "A: 20, B: 35, C: 15" or "Rojo: 10, Azul: 25"
     * - Numerical: "12, 15, 18.5, 20, 22, 25, 28, 30"
     */
    fun analyze(input: String): StatisticsAnalysisResult {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return generateEmptyResult(trimmed)
        }

        // 1. Check if input represents Bivariate pairs (x, y)
        val bivariatePairs = parseBivariateInput(trimmed)
        if (bivariatePairs.isNotEmpty() && bivariatePairs.size >= 2) {
            val bStats = calculateBivariateStats(bivariatePairs)
            val recommendation = recommendChartForBivariate(bStats)
            val yNumbers = bivariatePairs.map { it.y }
            val descStats = calculateDescriptiveStats(yNumbers)
            val varProcedure = calculateVarianceProcedure(yNumbers, descStats.mean)
            return StatisticsAnalysisResult(
                rawInput = trimmed,
                dataType = StatDataType.BIVARIATE_PAIRS,
                rawNumbers = yNumbers,
                categoricalItems = emptyList(),
                bivariatePoints = bivariatePairs,
                descriptiveStats = descStats,
                frequencyTable = null,
                bivariateStats = bStats,
                varianceProcedure = varProcedure,
                recommendation = recommendation
            )
        }

        // 2. Check if input represents Categorical frequencies (Label: Value or Label=Value)
        val categoricalItems = parseCategoricalInput(trimmed)
        if (categoricalItems.isNotEmpty() && categoricalItems.size >= 2) {
            val totalFreq = categoricalItems.sumOf { it.frequency }
            val recommendation = recommendChartForCategorical(categoricalItems)
            
            // Build pseudo frequency table for categorical
            var cumAbs = 0
            var cumRel = 0.0
            val tableEntries = categoricalItems.mapIndexed { idx, item ->
                val fi = item.frequency.roundToInt()
                cumAbs += fi
                val hi = if (totalFreq > 0) item.frequency / totalFreq else 0.0
                cumRel += hi
                FrequencyTableEntry(
                    index = idx + 1,
                    label = item.label,
                    lowerBound = idx.toDouble(),
                    upperBound = (idx + 1).toDouble(),
                    classMark = idx + 0.5,
                    absoluteFrequency = fi,
                    cumulativeAbsolute = cumAbs,
                    relativeFrequency = hi,
                    cumulativeRelative = cumRel,
                    percentage = hi * 100.0,
                    pieAngleDegrees = hi * 360.0
                )
            }
            val freqTable = FrequencyTable(
                entries = tableEntries,
                totalCount = totalFreq.roundToInt(),
                numClasses = tableEntries.size,
                classWidth = 1.0,
                minVal = 0.0,
                maxVal = tableEntries.size.toDouble()
            )

            return StatisticsAnalysisResult(
                rawInput = trimmed,
                dataType = StatDataType.CATEGORICAL_FREQUENCY,
                rawNumbers = categoricalItems.map { it.frequency },
                categoricalItems = categoricalItems,
                bivariatePoints = emptyList(),
                descriptiveStats = null,
                frequencyTable = freqTable,
                bivariateStats = null,
                varianceProcedure = null,
                recommendation = recommendation
            )
        }

        // 3. Fallback: Parse as Numerical series
        val numbers = parseNumbersList(trimmed)
        if (numbers.isEmpty()) {
            return generateEmptyResult(trimmed)
        }

        val descStats = calculateDescriptiveStats(numbers)
        val freqTable = calculateFrequencyTable(numbers)
        val recommendation = recommendChartForNumerical(numbers, descStats, freqTable)
        val varProcedure = calculateVarianceProcedure(numbers, descStats.mean)

        return StatisticsAnalysisResult(
            rawInput = trimmed,
            dataType = StatDataType.NUMERICAL_UNIVARIATE,
            rawNumbers = numbers,
            categoricalItems = emptyList(),
            bivariatePoints = numbers.mapIndexed { i, num -> BivariatePoint((i + 1).toDouble(), num) },
            descriptiveStats = descStats,
            frequencyTable = freqTable,
            bivariateStats = null,
            varianceProcedure = varProcedure,
            recommendation = recommendation
        )
    }

    // --- Parser Helpers ---

    private fun parseNumbersList(text: String): List<Double> {
        val clean = text.replace(";", ",").replace("\n", ",")
        return clean.split(",")
            .mapNotNull { token ->
                token.trim().toDoubleOrNull()
            }
    }

    private fun parseCategoricalInput(text: String): List<CategoricalItem> {
        val list = mutableListOf<CategoricalItem>()
        val lines = text.split(Regex("[,;\n]"))
        for (line in lines) {
            val part = line.trim()
            if (part.isEmpty()) continue
            if (part.contains(":") || part.contains("=")) {
                val delimiter = if (part.contains(":")) ":" else "="
                val label = part.substringBefore(delimiter).trim()
                val valueStr = part.substringAfter(delimiter).trim()
                val value = valueStr.toDoubleOrNull()
                if (label.isNotEmpty() && value != null && value >= 0) {
                    list.add(
                        CategoricalItem(
                            label = label,
                            frequency = value,
                            percentage = 0.0,
                            colorHex = PALETTE_COLORS[list.size % PALETTE_COLORS.size]
                        )
                    )
                }
            }
        }
        if (list.size >= 2) {
            val sum = list.sumOf { it.frequency }
            return list.map {
                it.copy(percentage = if (sum > 0) (it.frequency / sum) * 100.0 else 0.0)
            }
        }
        return emptyList()
    }

    private fun parseBivariateInput(text: String): List<BivariatePoint> {
        val list = mutableListOf<BivariatePoint>()
        // Match patterns like (1, 2.5) or (1; 2.5) or 1 2.5 or 1,2.5
        val parenthesisRegex = Regex("""\(\s*([-\d.]+)\s*[,;\s]\s*([-\d.]+)\s*\)""")
        val matches = parenthesisRegex.findAll(text).toList()
        if (matches.size >= 2) {
            for (m in matches) {
                val x = m.groupValues[1].toDoubleOrNull()
                val y = m.groupValues[2].toDoubleOrNull()
                if (x != null && y != null) {
                    list.add(BivariatePoint(x, y))
                }
            }
            return list
        }

        // Try lines of "x y" or "x, y"
        val lines = text.split("\n")
        if (lines.size >= 2) {
            val temp = mutableListOf<BivariatePoint>()
            for (l in lines) {
                val t = l.trim()
                if (t.isEmpty()) continue
                val parts = t.split(Regex("[,;\t ]+")).filter { it.isNotBlank() }
                if (parts.size == 2) {
                    val x = parts[0].toDoubleOrNull()
                    val y = parts[1].toDoubleOrNull()
                    if (x != null && y != null) {
                        temp.add(BivariatePoint(x, y))
                    }
                }
            }
            if (temp.size >= 2) return temp
        }

        return emptyList()
    }

    // --- Descriptive Statistics Engine ---

    fun calculateDescriptiveStats(numbers: List<Double>): DescriptiveStats {
        val n = numbers.size
        if (n == 0) return generateEmptyDescriptiveStats()

        val sorted = numbers.sorted()
        val sum = sorted.sum()
        val mean = sum / n
        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        // Median (Q2)
        val median = calculatePercentile(sorted, 50.0)

        // Quartiles
        val q1 = calculatePercentile(sorted, 25.0)
        val q2 = median
        val q3 = calculatePercentile(sorted, 75.0)
        val iqr = max(0.0, q3 - q1)

        // Outlier detection: Tukey's fences
        val lowerFence = q1 - 1.5 * iqr
        val upperFence = q3 + 1.5 * iqr
        val outliers = sorted.filter { it < lowerFence || it > upperFence }

        // Variances & Std Devs
        val sumSquaredDiffs = sorted.sumOf { (it - mean).pow(2) }
        val sampleVariance = if (n > 1) sumSquaredDiffs / (n - 1) else 0.0
        val populationVariance = sumSquaredDiffs / n
        val sampleStdDev = sqrt(sampleVariance)
        val populationStdDev = sqrt(populationVariance)

        // Mean Absolute Deviation (MAD)
        val meanAbsoluteDeviation = sorted.sumOf { abs(it - mean) } / n

        // Coefficient of Variation
        val cv = if (abs(mean) > 1e-9) (sampleStdDev / abs(mean)) * 100.0 else 0.0

        // Geometric & Harmonic Means (only for positive numbers)
        val allPositive = sorted.all { it > 0 }
        val geometricMean = if (allPositive) {
            exp(sorted.sumOf { ln(it) } / n)
        } else null

        val harmonicMean = if (allPositive) {
            n / sorted.sumOf { 1.0 / it }
        } else null

        // Modes Calculation
        val freqMap = mutableMapOf<Double, Int>()
        for (num in sorted) {
            // Round to 3 decimal places to cluster close floats
            val key = (num * 1000).roundToLong() / 1000.0
            freqMap[key] = (freqMap[key] ?: 0) + 1
        }
        val maxFreq = freqMap.values.maxOrNull() ?: 1
        val modes: List<Double>
        val modeDesc: String
        if (maxFreq == 1 || freqMap.size == 1 && n > 1) {
            modes = emptyList()
            modeDesc = "Amodal (Todos los valores tienen frecuencia 1)"
        } else {
            val candidateModes = freqMap.filter { it.value == maxFreq }.keys.toList()
            if (candidateModes.size == freqMap.size) {
                modes = emptyList()
                modeDesc = "Amodal (Distribución uniforme)"
            } else {
                modes = candidateModes
                modeDesc = when (modes.size) {
                    1 -> "Unimodal (Mo = ${formatNum(modes[0])}, frec = $maxFreq)"
                    2 -> "Bimodal (Mo = ${modes.joinToString(", ") { formatNum(it) }})"
                    else -> "Multimodal (${modes.size} modas con frec = $maxFreq)"
                }
            }
        }

        // Skewness (Fisher-Pearson)
        val skewness = if (n > 2 && sampleStdDev > 1e-9) {
            val m3 = sorted.sumOf { (it - mean).pow(3) } / n
            m3 / sampleStdDev.pow(3)
        } else 0.0

        // Kurtosis
        val kurtosis = if (n > 3 && sampleStdDev > 1e-9) {
            val m4 = sorted.sumOf { (it - mean).pow(4) } / n
            (m4 / sampleStdDev.pow(4)) - 3.0 // Excess kurtosis
        } else 0.0

        return DescriptiveStats(
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            modes = modes,
            modeDescription = modeDesc,
            min = min,
            max = max,
            range = range,
            sampleVariance = sampleVariance,
            populationVariance = populationVariance,
            sampleStdDev = sampleStdDev,
            populationStdDev = populationStdDev,
            meanAbsoluteDeviation = meanAbsoluteDeviation,
            coefficientOfVariation = cv,
            geometricMean = geometricMean,
            harmonicMean = harmonicMean,
            q1 = q1,
            q2 = q2,
            q3 = q3,
            iqr = iqr,
            lowerFence = lowerFence,
            upperFence = upperFence,
            outliers = outliers,
            skewness = skewness,
            kurtosis = kurtosis
        )
    }

    private fun calculatePercentile(sorted: List<Double>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        if (sorted.size == 1) return sorted[0]
        val rank = (p / 100.0) * (sorted.size - 1)
        val lowerIdx = floor(rank).toInt()
        val upperIdx = ceil(rank).toInt()
        if (lowerIdx == upperIdx) return sorted[lowerIdx]
        val fraction = rank - lowerIdx
        return sorted[lowerIdx] + fraction * (sorted[upperIdx] - sorted[lowerIdx])
    }

    // --- Frequency Distribution Table (Sturges Rule) ---

    fun calculateFrequencyTable(numbers: List<Double>): FrequencyTable {
        val n = numbers.size
        if (n == 0) {
            return FrequencyTable(emptyList(), 0, 0, 0.0, 0.0, 0.0)
        }
        val min = numbers.minOrNull() ?: 0.0
        val max = numbers.maxOrNull() ?: 0.0
        val range = max - min

        // Check if data is discrete with few unique values (e.g. dice rolls, small integers)
        val uniqueVals = numbers.distinct().sorted()
        if (uniqueVals.size <= 10 && range < 15 && uniqueVals.all { it == floor(it) }) {
            // Discrete tabular grouping
            var cumAbs = 0
            var cumRel = 0.0
            val entries = uniqueVals.mapIndexed { idx, valItem ->
                val count = numbers.count { it == valItem }
                cumAbs += count
                val hi = count.toDouble() / n
                cumRel += hi
                FrequencyTableEntry(
                    index = idx + 1,
                    label = formatNum(valItem),
                    lowerBound = valItem,
                    upperBound = valItem,
                    classMark = valItem,
                    absoluteFrequency = count,
                    cumulativeAbsolute = cumAbs,
                    relativeFrequency = hi,
                    cumulativeRelative = cumRel,
                    percentage = hi * 100.0,
                    pieAngleDegrees = hi * 360.0
                )
            }
            return FrequencyTable(
                entries = entries,
                totalCount = n,
                numClasses = entries.size,
                classWidth = 1.0,
                minVal = min,
                maxVal = max
            )
        }

        // Sturges' rule for continuous/grouped data: k = 1 + 3.322 * log10(n)
        val kRaw = 1.0 + 3.322 * log10(n.toDouble())
        val k = max(4, min(12, ceil(kRaw).toInt()))
        val rawWidth = if (range > 0) range / k else 1.0
        val classWidth = roundToNiceNumber(rawWidth)

        val entries = mutableListOf<FrequencyTableEntry>()
        var currentLower = floor(min / classWidth) * classWidth
        if (currentLower > min) currentLower = min

        var cumAbs = 0
        var cumRel = 0.0

        for (i in 0 until k) {
            val lower = currentLower + i * classWidth
            val upper = lower + classWidth
            val classMark = (lower + upper) / 2.0

            val count = if (i == k - 1) {
                // Include upper bound on last interval
                numbers.count { it in lower..upper }
            } else {
                numbers.count { it >= lower && it < upper }
            }

            cumAbs += count
            val hi = count.toDouble() / n
            cumRel += hi

            entries.add(
                FrequencyTableEntry(
                    index = i + 1,
                    label = "[${formatNum(lower)} - ${formatNum(upper)})",
                    lowerBound = lower,
                    upperBound = upper,
                    classMark = classMark,
                    absoluteFrequency = count,
                    cumulativeAbsolute = cumAbs,
                    relativeFrequency = hi,
                    cumulativeRelative = cumRel,
                    percentage = hi * 100.0,
                    pieAngleDegrees = hi * 360.0
                )
            )
        }

        return FrequencyTable(
            entries = entries,
            totalCount = n,
            numClasses = k,
            classWidth = classWidth,
            minVal = min,
            maxVal = max
        )
    }

    private fun roundToNiceNumber(value: Double): Double {
        if (value <= 0) return 1.0
        val exponent = floor(log10(value))
        val fraction = value / 10.0.pow(exponent)
        val niceFraction = when {
            fraction <= 1.2 -> 1.0
            fraction <= 2.2 -> 2.0
            fraction <= 3.0 -> 2.5
            fraction <= 7.0 -> 5.0
            else -> 10.0
        }
        return niceFraction * 10.0.pow(exponent)
    }

    // --- Bivariate Correlation & Linear Regression ---

    fun calculateBivariateStats(points: List<BivariatePoint>): BivariateStats {
        val n = points.size
        if (n < 2) return generateEmptyBivariateStats()

        val meanX = points.sumOf { it.x } / n
        val meanY = points.sumOf { it.y } / n

        val sumSqX = points.sumOf { (it.x - meanX).pow(2) }
        val sumSqY = points.sumOf { (it.y - meanY).pow(2) }
        val sumProdXY = points.sumOf { (it.x - meanX) * (it.y - meanY) }

        val varX = sumSqX / (n - 1)
        val varY = sumSqY / (n - 1)
        val stdDevX = sqrt(varX)
        val stdDevY = sqrt(varY)

        val covariance = sumProdXY / (n - 1)
        val pearsonR = if (stdDevX > 1e-9 && stdDevY > 1e-9) {
            covariance / (stdDevX * stdDevY)
        } else 0.0

        val rSquared = pearsonR.pow(2) * 100.0

        // Linear Regression: y = mx + b
        val slope = if (sumSqX > 1e-9) sumProdXY / sumSqX else 0.0
        val intercept = meanY - slope * meanX

        val sign = if (intercept >= 0) "+ " else "- "
        val regEq = "y = ${formatNum(slope)}x $sign${formatNum(abs(intercept))}"

        val (corrType, corrInterp) = diagnoseCorrelation(pearsonR, rSquared)

        return BivariateStats(
            points = points,
            count = n,
            meanX = meanX,
            meanY = meanY,
            varianceX = varX,
            varianceY = varY,
            stdDevX = stdDevX,
            stdDevY = stdDevY,
            covariance = covariance,
            pearsonR = pearsonR,
            rSquared = rSquared,
            slope = slope,
            intercept = intercept,
            regressionEquation = regEq,
            correlationType = corrType,
            correlationInterpretation = corrInterp
        )
    }

    private fun diagnoseCorrelation(r: Double, r2: Double): Pair<String, String> {
        val absR = abs(r)
        val type = when {
            absR >= 0.9 -> if (r > 0) "Correlación Positiva Muy Fuerte" else "Correlación Negativa Muy Fuerte"
            absR >= 0.7 -> if (r > 0) "Correlación Positiva Fuerte" else "Correlación Negativa Fuerte"
            absR >= 0.4 -> if (r > 0) "Correlación Positiva Moderada" else "Correlación Negativa Moderada"
            absR >= 0.2 -> if (r > 0) "Correlación Positiva Débil" else "Correlación Negativa Débil"
            else -> "Correlación Nula o Prácticamente Inexistente"
        }
        val interp = "El coeficiente de determinación R² = ${formatNum(r2)}% indica que el ${formatNum(r2)}% de la variabilidad de Y se explica linealmente mediante X."
        return Pair(type, interp)
    }

    // --- Intelligent Automatic Chart Recommender ---

    fun recommendChartForBivariate(stats: BivariateStats): ChartRecommendation {
        return ChartRecommendation(
            recommendedType = RecommendedChartType.SCATTER_PLOT,
            confidence = 0.98f,
            title = "✨ Recomendado: Diagrama de Dispersión (Scatter Plot)",
            reason = "Los datos ingresados consisten en pares ordenados de dos variables continuas (X, Y). El diagrama de dispersión es el estándar estadístico para analizar relaciones bivariadas, correlación de Pearson y trazar la recta de regresión lineal.",
            whyNotOthers = "Los gráficos univariados (como barras o circulares) no permiten observar la covarianza, dispersión o tendencia conjunta entre dos variables.",
            alternativeTypes = listOf(
                RecommendedChartType.FREQUENCY_POLYGON,
                RecommendedChartType.BAR_CHART
            )
        )
    }

    fun recommendChartForCategorical(items: List<CategoricalItem>): ChartRecommendation {
        val count = items.size
        return if (count in 2..7) {
            ChartRecommendation(
                recommendedType = RecommendedChartType.PIE_CHART,
                confidence = 0.95f,
                title = "✨ Recomendado: Gráfico Circular / Diagrama de Sectores",
                reason = "Se detectaron categorías cualitativas o nominales con frecuencias que componen un todo (100%). Con ${count} categorías, el gráfico circular ofrece la visualización más intuitiva de proporciones y porcentajes relativos.",
                whyNotOthers = "El histograma y el diagrama de dispersión requieren variables cuantitativas o pares continuos.",
                alternativeTypes = listOf(
                    RecommendedChartType.BAR_CHART
                )
            )
        } else {
            ChartRecommendation(
                recommendedType = RecommendedChartType.BAR_CHART,
                confidence = 0.92f,
                title = "✨ Recomendado: Diagrama de Barras",
                reason = "Al tener ${count} categorías, un diagrama de barras permite comparar con mayor precisión las alturas de cada frecuencia sin saturar la lectura angular.",
                whyNotOthers = "Con más de 7 categorías, los sectores de un gráfico circular se vuelven demasiado estrechos para distinguir diferencias sutiles.",
                alternativeTypes = listOf(
                    RecommendedChartType.PIE_CHART
                )
            )
        }
    }

    fun recommendChartForNumerical(
        numbers: List<Double>,
        stats: DescriptiveStats,
        freqTable: FrequencyTable
    ): ChartRecommendation {
        val n = numbers.size
        val uniqueCount = numbers.distinct().size

        // Case A: Few discrete integer values (e.g., scores 1,2,3,4,5 or categories)
        if (uniqueCount <= 6 && numbers.all { it == floor(it) } && stats.range <= 10) {
            return ChartRecommendation(
                recommendedType = RecommendedChartType.BAR_CHART,
                confidence = 0.90f,
                title = "✨ Recomendado: Diagrama de Barras Discretas",
                reason = "Tus datos son cuantitativos discretos con sólo ${uniqueCount} valores distintos repetidos. Las barras separadas representan claramente la frecuencia exacta de cada valor individual.",
                whyNotOthers = "Un histograma está diseñado para intervalos continuos agrupados, mientras que aquí cada número entero es una categoría discreta exacta.",
                alternativeTypes = listOf(
                    RecommendedChartType.PIE_CHART,
                    RecommendedChartType.FREQUENCY_POLYGON,
                    RecommendedChartType.BOX_PLOT
                )
            )
        }

        // Case B: Many continuous data points -> Histogram or Frequency Polygon
        if (n >= 15 || uniqueCount >= 8) {
            return ChartRecommendation(
                recommendedType = RecommendedChartType.HISTOGRAM,
                confidence = 0.96f,
                title = "✨ Recomendado: Histograma de Frecuencias (Sturges)",
                reason = "Tienes una muestra continua de ${n} valores. El histograma agrupa los datos en ${freqTable.numClasses} intervalos de clase contiguos (según la Regla de Sturges) para revelar la forma de la distribución, simetría y concentración modal.",
                whyNotOthers = "Un gráfico circular resultaría ilegible con tantos valores dispersos, y un diagrama de dispersión requiere una segunda variable Y.",
                alternativeTypes = listOf(
                    RecommendedChartType.FREQUENCY_POLYGON,
                    RecommendedChartType.BOX_PLOT,
                    RecommendedChartType.BAR_CHART
                )
            )
        }

        // Case C: Moderate numerical series -> Box plot or Polygon
        return ChartRecommendation(
            recommendedType = RecommendedChartType.FREQUENCY_POLYGON,
            confidence = 0.88f,
            title = "✨ Recomendado: Polígono de Frecuencias",
            reason = "Muestra la tendencia y silueta continua de la distribución uniendo las marcas de clase de cada intervalo. Es ideal para comparar el perfil de densidad.",
            whyNotOthers = "Permite apreciar la continuidad y los puntos de máxima densidad sin la rigidez de barras separadas.",
            alternativeTypes = listOf(
                RecommendedChartType.HISTOGRAM,
                RecommendedChartType.BOX_PLOT,
                RecommendedChartType.BAR_CHART
            )
        )
    }

    fun calculateVarianceProcedure(numbers: List<Double>, mean: Double): VarianceProcedure? {
        val n = numbers.size
        if (n == 0) return null

        var sumDev = 0.0
        var sumSqDev = 0.0
        val items = numbers.mapIndexed { idx, x ->
            val dev = x - mean
            sumDev += dev
            val sqDev = dev.pow(2)
            sumSqDev += sqDev

            val devSignStr = if (dev >= 0) "+${formatNum(dev)}" else formatNum(dev)
            val devCalc = "${formatNum(x)} - ${formatNum(mean)} = $devSignStr"
            val sqCalc = "(${formatNum(dev)})² = ${formatNum(sqDev)}"

            VarianceStepItem(
                index = idx + 1,
                x = x,
                xStr = formatNum(x),
                deviation = dev,
                deviationCalcStr = devCalc,
                deviationFormatted = devSignStr,
                squaredDeviation = sqDev,
                squaredCalcStr = sqCalc,
                squaredFormatted = formatNum(sqDev)
            )
        }

        // Population calculations (N)
        val popVar = sumSqDev / n
        val popStdDev = sqrt(popVar)

        // Sample calculations (n - 1)
        val sampVar = if (n > 1) sumSqDev / (n - 1) else 0.0
        val sampStdDev = sqrt(sampVar)

        return VarianceProcedure(
            mean = mean,
            meanFormatted = formatNum(mean),
            count = n,
            items = items,
            sumDeviations = sumDev,
            sumDeviationsFormatted = if (abs(sumDev) < 1e-4) "0" else formatNum(sumDev),
            sumSquaredDeviations = sumSqDev,
            sumSquaredDeviationsFormatted = formatNum(sumSqDev),
            populationVarianceFormula = "σ² = Σ(x - μ)² / N",
            populationVarianceFraction = "${formatNum(sumSqDev)} / $n",
            populationVarianceValue = popVar,
            populationVarianceFormatted = formatNum(popVar),
            populationStdDevFormula = "σ = √${formatNum(popVar)}",
            populationStdDevValue = popStdDev,
            populationStdDevFormatted = formatNum(popStdDev),
            sampleVarianceFormula = "s² = Σ(x - x̄)² / (n - 1)",
            sampleVarianceFraction = if (n > 1) "${formatNum(sumSqDev)} / ($n - 1) = ${formatNum(sumSqDev)} / ${n - 1}" else "${formatNum(sumSqDev)} / 0",
            sampleVarianceValue = sampVar,
            sampleVarianceFormatted = formatNum(sampVar),
            sampleStdDevFormula = "s = √${formatNum(sampVar)}",
            sampleStdDevValue = sampStdDev,
            sampleStdDevFormatted = formatNum(sampStdDev)
        )
    }

    private fun generateEmptyResult(rawInput: String): StatisticsAnalysisResult {
        return StatisticsAnalysisResult(
            rawInput = rawInput,
            dataType = StatDataType.NUMERICAL_UNIVARIATE,
            rawNumbers = emptyList(),
            categoricalItems = emptyList(),
            bivariatePoints = emptyList(),
            descriptiveStats = null,
            frequencyTable = null,
            bivariateStats = null,
            varianceProcedure = null,
            recommendation = ChartRecommendation(
                recommendedType = RecommendedChartType.BAR_CHART,
                confidence = 0.5f,
                title = "Ingresa datos para recibir recomendaciones automáticas",
                reason = "Escribe una lista de números, categorías (Ej: A: 10, B: 25) o pares (X, Y).",
                whyNotOthers = "",
                alternativeTypes = emptyList()
            )
        )
    }

    private fun generateEmptyDescriptiveStats(): DescriptiveStats {
        return DescriptiveStats(
            count = 0, sum = 0.0, mean = 0.0, median = 0.0, modes = emptyList(),
            modeDescription = "-", min = 0.0, max = 0.0, range = 0.0,
            sampleVariance = 0.0, populationVariance = 0.0, sampleStdDev = 0.0,
            populationStdDev = 0.0, meanAbsoluteDeviation = 0.0, coefficientOfVariation = 0.0,
            geometricMean = null, harmonicMean = null, q1 = 0.0, q2 = 0.0, q3 = 0.0,
            iqr = 0.0, lowerFence = 0.0, upperFence = 0.0, outliers = emptyList(),
            skewness = 0.0, kurtosis = 0.0
        )
    }

    private fun generateEmptyBivariateStats(): BivariateStats {
        return BivariateStats(
            points = emptyList(), count = 0, meanX = 0.0, meanY = 0.0,
            varianceX = 0.0, varianceY = 0.0, stdDevX = 0.0, stdDevY = 0.0,
            covariance = 0.0, pearsonR = 0.0, rSquared = 0.0, slope = 0.0,
            intercept = 0.0, regressionEquation = "y = 0", correlationType = "-",
            correlationInterpretation = "-"
        )
    }

    fun formatNum(value: Double): String {
        return if (value.isNaN() || value.isInfinite()) "0"
        else if (abs(value - value.roundToLong()) < 1e-5) {
            value.roundToLong().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }
}
