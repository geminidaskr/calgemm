package com.example.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.PhotoMathResult
import com.example.ai.PrecalcAiService
import com.example.math.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PrecalcViewModel : ViewModel() {

    // --- Graph & Functions State ---
    private val _functions = MutableStateFlow<List<FunctionDefinition>>(
        listOf(
            FunctionDefinition(name = "f(x)", expression = "x^2 - 4", colorHex = 0xFF88C0D0, isVisible = true)
        )
    )
    val functions: StateFlow<List<FunctionDefinition>> = _functions.asStateFlow()

    private val _currentExpression = MutableStateFlow("x^2 - 4")
    val currentExpression: StateFlow<String> = _currentExpression.asStateFlow()

    private val _viewport = MutableStateFlow(GraphViewport(-10f, 10f, -10f, 10f))
    val viewport: StateFlow<GraphViewport> = _viewport.asStateFlow()

    private val _analysis = MutableStateFlow<FunctionAnalysis?>(null)
    val analysis: StateFlow<FunctionAnalysis?> = _analysis.asStateFlow()

    // --- Equation Solver State ---
    private val _equationInput = MutableStateFlow("x^2 - 5x + 6 = 0")
    val equationInput: StateFlow<String> = _equationInput.asStateFlow()

    private val _equationSolution = MutableStateFlow<EquationSolution?>(null)
    val equationSolution: StateFlow<EquationSolution?> = _equationSolution.asStateFlow()

    // --- Inverse Function Solver State ---
    private val _inverseFunctionInput = MutableStateFlow("(2x + 1) / (x - 3)")
    val inverseFunctionInput: StateFlow<String> = _inverseFunctionInput.asStateFlow()

    private val _inverseSolution = MutableStateFlow<InverseFunctionSolution?>(null)
    val inverseSolution: StateFlow<InverseFunctionSolution?> = _inverseSolution.asStateFlow()

    // --- Statistics & Charting State ---
    private val _statInput = MutableStateFlow("14, 18, 12, 19, 15, 17, 13, 20, 16, 18, 14, 15, 17, 19, 11, 16, 18, 13, 15, 20")
    val statInput: StateFlow<String> = _statInput.asStateFlow()

    private val _statResult = MutableStateFlow<StatisticsAnalysisResult?>(null)
    val statResult: StateFlow<StatisticsAnalysisResult?> = _statResult.asStateFlow()

    private val _selectedChartType = MutableStateFlow<RecommendedChartType?>(null)
    val selectedChartType: StateFlow<RecommendedChartType?> = _selectedChartType.asStateFlow()

    // --- AI Tutor State ---
    private val _aiPrompt = MutableStateFlow("")
    val aiPrompt: StateFlow<String> = _aiPrompt.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Camera / Photo Math State ---
    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    private val _isPhotoSolving = MutableStateFlow(false)
    val isPhotoSolving: StateFlow<Boolean> = _isPhotoSolving.asStateFlow()

    private val _photoMathResult = MutableStateFlow<PhotoMathResult?>(null)
    val photoMathResult: StateFlow<PhotoMathResult?> = _photoMathResult.asStateFlow()

    private val _photoErrorMessage = MutableStateFlow<String?>(null)
    val photoErrorMessage: StateFlow<String?> = _photoErrorMessage.asStateFlow()

    // --- History State ---
    private val _history = MutableStateFlow<List<HistoryItem>>(
        listOf(
            HistoryItem(
                expression = "f(x) = (2x + 1) / (x - 3)",
                type = "FUNCIÓN (FOTO)",
                resultPreview = "Inversa f⁻¹(x) = (3x + 1)/(x - 2), asíntotas en x=3 y y=2"
            ),
            HistoryItem(
                expression = "x^2 - 5x + 6 = 0",
                type = "ECUACIÓN (FOTO)",
                resultPreview = "Soluciones reales: x₁ = 2, x₂ = 3"
            ),
            HistoryItem(
                expression = "√(2x + 5) = 3",
                type = "ECUACIÓN (FOTO)",
                resultPreview = "Solución exacta: x = 2 (comprobada en radical)"
            )
        )
    )
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    // --- Theme State (true = Dark, false = Light) ---
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        // Run initial analysis
        analyzeCurrentFunction()
        solveCurrentEquation()
        solveCurrentInverse()
        analyzeStatistics()
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setExpression(expr: String) {
        _currentExpression.value = expr
        updatePrimaryFunction(expr)
    }

    fun appendToExpression(text: String) {
        _currentExpression.value += text
        updatePrimaryFunction(_currentExpression.value)
    }

    fun backspaceExpression() {
        if (_currentExpression.value.isNotEmpty()) {
            _currentExpression.value = _currentExpression.value.dropLast(1)
            updatePrimaryFunction(_currentExpression.value)
        }
    }

    fun clearExpression() {
        _currentExpression.value = ""
        updatePrimaryFunction("")
    }

    private fun updatePrimaryFunction(expr: String) {
        val list = _functions.value.toMutableList()
        if (list.isNotEmpty()) {
            list[0] = list[0].copy(expression = expr)
        } else {
            list.add(FunctionDefinition(expression = expr))
        }
        _functions.value = list
        analyzeCurrentFunction()
    }

    fun addSecondaryFunction(name: String, expr: String, colorHex: Long) {
        val list = _functions.value.toMutableList()
        list.add(FunctionDefinition(name = name, expression = expr, colorHex = colorHex))
        _functions.value = list
    }

    fun toggleFunctionVisibility(id: String) {
        _functions.value = _functions.value.map {
            if (it.id == id) it.copy(isVisible = !it.isVisible) else it
        }
    }

    fun removeFunction(id: String) {
        if (_functions.value.size > 1) {
            _functions.value = _functions.value.filter { it.id != id }
        }
    }

    fun updateViewport(newViewport: GraphViewport) {
        _viewport.value = newViewport
    }

    fun analyzeCurrentFunction() {
        val expr = _currentExpression.value.trim()
        if (expr.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val result = PrecalcSolvers.analyzeFunction(expr)
                    _analysis.value = result
                    addToHistory(expr, "FUNCIÓN", "Dom: ${result.domain}")
                } catch (_: Exception) {
                    // Ignore errors during incomplete typing
                }
            }
        }
    }

    // --- Equation Solver Functions ---
    fun setEquationInput(eq: String) {
        _equationInput.value = eq
    }

    fun appendToEquation(text: String) {
        _equationInput.value += text
    }

    fun backspaceEquation() {
        if (_equationInput.value.isNotEmpty()) {
            _equationInput.value = _equationInput.value.dropLast(1)
        }
    }

    fun clearEquation() {
        _equationInput.value = ""
    }

    fun solveCurrentEquation() {
        val eq = _equationInput.value.trim()
        if (eq.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val solution = PrecalcSolvers.solveEquationStepByStep(eq)
                    _equationSolution.value = solution
                    addToHistory(eq, "ECUACIÓN", solution.solutions.joinToString(", "))
                } catch (_: Exception) {
                    // Ignore transient typing error
                }
            }
        }
    }

    // --- Inverse Function Solver Functions ---
    fun setInverseInput(fn: String) {
        _inverseFunctionInput.value = fn
    }

    fun appendToInverse(text: String) {
        _inverseFunctionInput.value += text
    }

    fun backspaceInverse() {
        if (_inverseFunctionInput.value.isNotEmpty()) {
            _inverseFunctionInput.value = _inverseFunctionInput.value.dropLast(1)
        }
    }

    fun clearInverse() {
        _inverseFunctionInput.value = ""
    }

    fun solveCurrentInverse() {
        val fn = _inverseFunctionInput.value.trim()
        if (fn.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val solution = InverseFunctionSolver.solveInverse(fn)
                    _inverseSolution.value = solution
                    addToHistory(fn, "FUNCIÓN INVERSA", solution.inverseFunctionExpression)
                } catch (_: Exception) {
                    // Ignore transient errors during typing
                }
            }
        }
    }

    // --- Statistics & Charting Functions ---
    fun setStatInput(input: String) {
        _statInput.value = input
        analyzeStatistics()
    }

    fun selectChartType(type: RecommendedChartType?) {
        _selectedChartType.value = type
    }

    fun analyzeStatistics() {
        val input = _statInput.value
        try {
            val result = StatisticsCalculator.analyze(input)
            _statResult.value = result
            if (_selectedChartType.value == null || !isValidForCurrentData(_selectedChartType.value, result)) {
                _selectedChartType.value = result.recommendation.recommendedType
            }
        } catch (_: Exception) {
            // Ignore transient typing error
        }
    }

    private fun isValidForCurrentData(chart: RecommendedChartType?, result: StatisticsAnalysisResult): Boolean {
        if (chart == null) return false
        return when (chart) {
            RecommendedChartType.SCATTER_PLOT -> result.bivariateStats != null || result.bivariatePoints.isNotEmpty()
            RecommendedChartType.PIE_CHART -> result.categoricalItems.isNotEmpty() || (result.frequencyTable != null && result.frequencyTable.entries.size <= 10)
            RecommendedChartType.HISTOGRAM -> result.frequencyTable != null && result.rawNumbers.size >= 4
            RecommendedChartType.FREQUENCY_POLYGON -> result.frequencyTable != null
            RecommendedChartType.BAR_CHART -> result.frequencyTable != null || result.categoricalItems.isNotEmpty()
            RecommendedChartType.BOX_PLOT -> result.descriptiveStats != null && result.descriptiveStats.count >= 3
        }
    }

    fun loadStatPreset(presetType: String) {
        val sample = when (presetType) {
            "varianza_ejemplo" -> "5, 7, 1, 2, 3"
            "calificaciones" -> "14, 18, 12, 19, 15, 17, 13, 20, 16, 18, 14, 15, 17, 19, 11, 16, 18, 13, 15, 20"
            "encuesta" -> "Matemáticas: 45, Física: 30, Química: 25, Programación: 50, Biología: 20"
            "horas_vs_nota" -> "(2, 11), (4, 13), (5, 14), (7, 17), (8, 18), (10, 20), (3, 12), (6, 15), (9, 19)"
            "edades" -> "18, 18, 19, 19, 19, 20, 20, 20, 20, 21, 21, 22, 22, 23"
            "ventas" -> "Trimestre 1: 12500, Trimestre 2: 18400, Trimestre 3: 15900, Trimestre 4: 22800"
            "pesos" -> "62.5, 68.0, 71.2, 59.8, 85.0, 64.3, 73.1, 66.4, 70.0, 82.5, 61.0, 69.4"
            else -> "10, 12, 15, 14, 18, 20, 22, 25, 28, 30"
        }
        _statInput.value = sample
        val result = StatisticsCalculator.analyze(sample)
        _statResult.value = result
        _selectedChartType.value = result.recommendation.recommendedType
        addToHistory("Estadística: $presetType", "ESTADÍSTICA Y GRÁFICO", result.recommendation.title)
    }

    // --- AI Tutor Functions ---
    fun setAiPrompt(prompt: String) {
        _aiPrompt.value = prompt
    }

    fun askAiTutor(customPrompt: String? = null) {
        val rawPrompt = customPrompt ?: _aiPrompt.value
        val prompt = PrecalcAiService.sanitizePrompt(rawPrompt)
        if (prompt.isBlank()) return

        _isAiLoading.value = true
        _aiResponse.value = null

        viewModelScope.launch {
            try {
                val res = PrecalcAiService.explainOrSolve(prompt)
                _isAiLoading.value = false
                res.onSuccess { text ->
                    _aiResponse.value = text
                }.onFailure { error ->
                    _aiResponse.value = """
                        ### ⚠️ Error en Tutor IA (Groq)
                        
                        **Detalle del fallo**:
                        `${error.message ?: "No se pudo conectar con el servicio de Groq."}`
                        
                        💡 *Verifica tu conexión a Internet o que tu clave `GROQ_API_KEY` esté configurada correctamente en el panel Secrets de AI Studio.*
                    """.trimIndent()
                }
            } catch (e: Throwable) {
                _isAiLoading.value = false
                _aiResponse.value = """
                    ### ⚠️ Error en Tutor IA (Groq)
                    
                    **Detalle del fallo**:
                    `${e.message ?: e.localizedMessage ?: e.javaClass.simpleName}`
                    
                    💡 *Verifica tu conexión a Internet o que tu clave `GROQ_API_KEY` esté configurada correctamente en el panel Secrets de AI Studio.*
                """.trimIndent()
            }
        }
    }

    // --- Camera / Photo Solver Functions ---
    fun setCapturedPhoto(bitmap: Bitmap?) {
        _capturedPhoto.value = bitmap
        _photoErrorMessage.value = null
    }

    fun solveProblemFromBitmap(bitmap: Bitmap, customPrompt: String? = null) {
        _capturedPhoto.value = bitmap
        _isPhotoSolving.value = true
        _photoMathResult.value = null
        _photoErrorMessage.value = null

        viewModelScope.launch {
            try {
                val result = PrecalcAiService.solveMathProblemFromImage(bitmap, customPrompt)
                _isPhotoSolving.value = false
                result.onSuccess { data ->
                    _photoMathResult.value = data
                    addToHistory(data.cleanExpression ?: data.transcribedProblem, if (data.isEquation) "ECUACIÓN (FOTO)" else "FUNCIÓN (FOTO)", "Resuelto con Cámara IA")
                }.onFailure { error ->
                    _photoErrorMessage.value = error.message ?: "No se pudo interpretar la imagen. Intenta con mejor iluminación o ángulo recto."
                }
            } catch (e: Exception) {
                _isPhotoSolving.value = false
                _photoErrorMessage.value = "Error al procesar la imagen: ${e.message}"
            }
        }
    }

    fun clearPhotoSolution() {
        _capturedPhoto.value = null
        _photoMathResult.value = null
        _photoErrorMessage.value = null
    }

    fun loadSampleMathBitmap(sampleType: String): Bitmap {
        val width = 600
        val height = 320
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#ECEFF4") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw math text on bitmap
        val textPaint = Paint().apply {
            color = Color.parseColor("#2E3440")
            textSize = 38f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#4C566A")
            textSize = 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val text = when (sampleType) {
            "inversa" -> "f(x) = (2x + 1) / (x - 3)"
            "racional" -> "f(x) = (2x + 1) / (x - 3)"
            "radical" -> "√(2x + 5) = 3"
            "trig" -> "sen²(x) + cos²(x) = 1"
            "log" -> "ln(x - 2) = 1"
            else -> "x² - 5x + 6 = 0"
        }

        val sub = "Problema de Precálculo (Foto de Muestra)"
        canvas.drawText(text, (width / 2).toFloat(), 140f, textPaint)
        canvas.drawText(sub, (width / 2).toFloat(), 210f, subtitlePaint)

        return bitmap
    }

    private fun addToHistory(expr: String, type: String, preview: String) {
        val current = _history.value.toMutableList()
        // Prevent immediate duplicates
        if (current.none { it.expression == expr }) {
            current.add(0, HistoryItem(expression = expr, type = type, resultPreview = preview))
            if (current.size > 20) {
                current.removeLast()
            }
            _history.value = current
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
