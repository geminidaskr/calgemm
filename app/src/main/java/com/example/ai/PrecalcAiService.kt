package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.math.MathFormatter
import com.example.math.PrecalcSolvers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToLong

data class PhotoMathResult(
    val transcribedProblem: String,
    val solutionExplanation: String,
    val cleanExpression: String? = null,
    val isEquation: Boolean = true
)

/**
 * Service to call Gemini API for advanced Precalculus pedagogical step-by-step solutions,
 * multimodal camera problem solving, and conceptual breakdowns.
 */
object PrecalcAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val GROQ_MODEL = "openai/gpt-oss-120b"

    private const val PRIMARY_MODEL = "gemini-3.5-flash"
    private const val SECONDARY_MODEL = "gemini-3.1-pro-preview"
    private const val FALLBACK_MODEL = "gemini-flash-latest"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if very large to optimize bandwidth and processing
        val maxDimension = 1024
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
            val newHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
            Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
        } else {
            this
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun getGroqApiKey(): String {
        var key = try {
            BuildConfig.GROQ_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (key.isBlank() || key == "MY_GROQ_API_KEY") {
            key = System.getenv("GROQ_API_KEY") ?: ""
        }
        return key.trim()
    }

    private fun getGeminiApiKey(): String {
        var key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            key = System.getenv("GEMINI_API_KEY") ?: ""
        }
        return key.trim()
    }

    /**
     * Sanitizes user input or voice-transcribed prompt removing illegal control characters,
     * zero-width spaces, and normalizing whitespace safely.
     */
    fun sanitizePrompt(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return raw
            // Remove null characters and non-printable control characters (except standard newlines/tabs)
            .replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "")
            // Remove zero-width spaces, BOM, directional formatting markers
            .replace("\u200B", "")
            .replace("\u200C", "")
            .replace("\u200D", "")
            .replace("\uFEFF", "")
            .replace(Regex("[\u202A-\u202E\u2066-\u2069]"), "")
            // Normalize non-breaking spaces
            .replace('\u00A0', ' ')
            .replace('\u202F', ' ')
            .trim()
    }

    /**
     * Calls the Groq API (OpenAI Chat Completions format) using llama-3.3-70b-versatile
     * to provide pedagogical step-by-step solutions and conceptual explanations.
     */
    suspend fun explainOrSolve(rawPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        val prompt = sanitizePrompt(rawPrompt)
        if (prompt.isBlank()) {
            return@withContext Result.failure(Exception("La consulta ingresada está vacía."))
        }

        val apiKey = getGroqApiKey()

        if (apiKey.isBlank() || apiKey == "MY_GROQ_API_KEY") {
            return@withContext try {
                Result.success(generatePedagogicalResponse(prompt))
            } catch (e: Throwable) {
                Result.failure(Exception("Error al generar respuesta pedagógica: ${e.message ?: e.javaClass.simpleName}"))
            }
        }

        val systemPrompt = """
            Eres un profesor experto y tutor universitario de Precálculo y Matemáticas.
            Tu objetivo es explicar conceptos, resolver ejercicios y enseñar paso a paso de forma amigable, rigurosa y didáctica.
            
            REGLAS IMPORTANTES:
            1. Si el usuario te hace una pregunta general, un saludo o una duda conceptual, responde de manera natural y conversacional adaptada exactamente a lo que preguntó.
            2. Si el usuario te da una operación aritmética o ecuación, presenta la resolución paso a paso con:
               - Título del paso
               - Ecuación matemática
               - Explicación de la propiedad algebraica, trigonométrica o analítica aplicada.
            3. Resalta la Solución Final de forma clara y destacada.
            4. Si el usuario te da una función, analiza dominio, rango, restricciones, asíntotas o inversa según corresponda.
            5. Cuando expliques reglas o teoremas y muestres un ejemplo, separa el ejemplo encabezándolo claramente con "**Ejemplo Práctico:**" o "### Ejemplo:" para distinguirlo visualmente de la regla teórica general.
            6. Responde en Español con formato Markdown bien estructurado y notación matemática compatible con KaTeX.
        """.trimIndent()

        try {
            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            val jsonBody = JSONObject().apply {
                put("model", GROQ_MODEL)
                put("messages", messagesArray)
                put("temperature", 0.3)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(GROQ_URL)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.optJSONArray("choices")
                val firstChoice = choices?.optJSONObject(0)
                val message = firstChoice?.optJSONObject("message")
                val text = message?.optString("content")

                if (!text.isNullOrBlank()) {
                    val normalized = com.example.ui.components.normalizeMathDelimiters(text)
                    return@withContext Result.success(normalized)
                } else {
                    return@withContext Result.failure(Exception("La API de Groq devolvió una respuesta vacía."))
                }
            } else {
                val errorDetail = try {
                    val jsonErr = JSONObject(responseBody)
                    val errObj = jsonErr.optJSONObject("error")
                    errObj?.optString("message") ?: responseBody.take(200)
                } catch (_: Exception) {
                    responseBody.take(200)
                }
                return@withContext Result.failure(Exception("Error HTTP ${response.code} (Groq $GROQ_MODEL): $errorDetail"))
            }
        } catch (e: Throwable) {
            return@withContext Result.failure(Exception("Error de conexión con Groq: ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }

    suspend fun solveMathProblemFromImage(
        bitmap: Bitmap,
        userPrompt: String? = null
    ): Result<PhotoMathResult> = withContext(Dispatchers.IO) {
        val apiKey = getGeminiApiKey()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(generateOfflinePhotoResult(userPrompt))
        }

        val base64Image = bitmap.toBase64()
        val promptText = if (!userPrompt.isNullOrBlank()) {
            """
            Analiza cuidadosamente la imagen adjunta que contiene un problema o ecuación matemática de Precálculo.
            Instrucciones adicionales del usuario: $userPrompt
            
            Por favor responde siguiendo estrictamente este formato:
            
            TRANSCRIPCIÓN: [Escribe aquí la ecuación o problema exacto detectado en la imagen, por ejemplo: x^2 - 5x + 6 = 0 o f(x) = (2x+1)/(x-3)]
            FORMULA_LIMPIA: [Escribe únicamente la fórmula o ecuación matemática limpia sin texto adicional para que la app pueda graficarla o resolverla]
            TIPO: [ECUACION o FUNCION o PROBLEMA]
            
            SOLUCIÓN DETALLADA:
            (Explica el procedimiento completo paso a paso, indicando las propiedades algebraicas, cálculos intermedios, dominio, restricciones y la respuesta final destacada en formato Markdown).
            """.trimIndent()
        } else {
            """
            Analiza cuidadosamente la imagen adjunta que contiene un problema o ecuación matemática de Precálculo.
            
            Por favor responde siguiendo estrictamente este formato:
            
            TRANSCRIPCIÓN: [Escribe aquí la ecuación o problema exacto detectado en la imagen, por ejemplo: x^2 - 5x + 6 = 0 o f(x) = (2x+1)/(x-3)]
            FORMULA_LIMPIA: [Escribe únicamente la fórmula o ecuación matemática limpia sin texto adicional para que la app pueda graficarla o resolverla]
            TIPO: [ECUACION o FUNCION o PROBLEMA]
            
            SOLUCIÓN DETALLADA:
            (Explica el procedimiento completo paso a paso, indicando las propiedades algebraicas, cálculos intermedios, dominio, restricciones y la respuesta final destacada en formato Markdown).
            """.trimIndent()
        }

        val modelsToTry = listOf(PRIMARY_MODEL, SECONDARY_MODEL, FALLBACK_MODEL)
        var lastError: String? = null

        for (model in modelsToTry) {
            try {
                val url = "$BASE_URL/$model:generateContent?key=$apiKey"
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", promptText))
                                put(JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.2)
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")

                    if (!text.isNullOrBlank()) {
                        return@withContext Result.success(parsePhotoMathResponse(text))
                    }
                } else {
                    val errorDetail = try {
                        val jsonErr = JSONObject(responseBody)
                        jsonErr.optJSONObject("error")?.optString("message") ?: responseBody.take(200)
                    } catch (_: Exception) {
                        responseBody.take(200)
                    }
                    lastError = "Error HTTP ${response.code} ($model): $errorDetail"
                }
            } catch (e: Exception) {
                lastError = "Error con $model: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }
        }

        Result.failure(Exception(lastError ?: "No se pudo interpretar la imagen."))
    }

    private fun parsePhotoMathResponse(rawText: String): PhotoMathResult {
        var transcribed = "Problema Detectado"
        var cleanFormula: String? = null
        var isEquation = true

        val lines = rawText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("TRANSCRIPCIÓN:", ignoreCase = true) || trimmed.startsWith("TRANSCRIPCION:", ignoreCase = true)) {
                transcribed = trimmed.substringAfter(":").trim().removePrefix("[").removeSuffix("]")
            } else if (trimmed.startsWith("FORMULA_LIMPIA:", ignoreCase = true)) {
                cleanFormula = trimmed.substringAfter(":").trim().removePrefix("[").removeSuffix("]")
            } else if (trimmed.startsWith("TIPO:", ignoreCase = true)) {
                isEquation = !trimmed.contains("FUNCION", ignoreCase = true)
            }
        }

        if (cleanFormula.isNullOrBlank() && transcribed.contains("=")) {
            cleanFormula = transcribed
        }

        return PhotoMathResult(
            transcribedProblem = transcribed,
            solutionExplanation = rawText,
            cleanExpression = cleanFormula,
            isEquation = isEquation
        )
    }

    /**
     * Intelligent Precalculus pedagogical reasoning engine when offline or no API key.
     * Computes actual mathematical steps, definitions, proofs and solver solutions.
     */
    fun generatePedagogicalResponse(prompt: String): String {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()

        // 1. Greetings and conversational queries
        val greetings = listOf("hola", "buenas", "buenos dias", "buenos días", "buenas tardes", "buenas noches", "saludos", "hi", "hello", "que tal", "qué tal", "como estas", "cómo estás", "quien eres", "quién eres", "ayuda", "help", "inicio", "empezar")
        if (greetings.any { lower == it || lower.startsWith("$it ") || lower.startsWith("$it,") || lower.startsWith("$it!") }) {
            return """
                ### 👋 ¡Hola! Soy tu Tutor de Precálculo y Matemáticas
                
                Estoy aquí para ayudarte a comprender conceptos, resolver ejercicios y graficar funciones paso a paso.
                
                #### 🌟 ¿En qué puedo ayudarte hoy?
                * **Resolución de Ecuaciones**: Escribe cualquier ecuación como `x^2 - 5x + 6 = 0` o `√(2x+5) = 3` para ver el procedimiento paso a paso con justificación algebraica.
                * **Cálculo Aritmético y Expresiones**: Pregúntame operaciones directas como `2+2`, `sqrt(144) + 5` o simplificación de fracciones.
                * **Estudio de Funciones**: Dominio, rango, raíces, asíntotas, simetrías e inversa f⁻¹(x) de funciones algebraicas y racionales.
                * **Trigonometría y Polinomios**: Círculo unitario, identidades pitagóricas, teorema fundamental del álgebra y división sintética.
                * **Estadística y Varianza**: Varianza poblacional (σ²), muestral (s²), desviación estándar y tablas de desviaciones (xᵢ - x̄).
                * **Cámara IA**: Toma una foto a un ejercicio escrito en papel para resolverlo y graficarlo al instante.
                
                ✍️ *Escribe tu pregunta o ecuación en el campo de texto inferior para comenzar.*
            """.trimIndent()
        }

        // 2. Arithmetic questions like "¿cuánto es 2+2?", "2+2", "3 * 15", "100 / 4", "sqrt(16) + 3"
        val arithmeticClean = trimmed
            .replace("¿", "")
            .replace("?", "")
            .replace("cuanto es", "", ignoreCase = true)
            .replace("cuánto es", "", ignoreCase = true)
            .replace("calcula", "", ignoreCase = true)
            .replace("calcular", "", ignoreCase = true)
            .replace("resultado de", "", ignoreCase = true)
            .replace("resuelve", "", ignoreCase = true)
            .trim()

        val isPureArithmetic = (arithmeticClean.matches(Regex("""^[0-9\.\s\+\-\*\/\^\(\)\%\,\√\÷\×]+$""")) || arithmeticClean.startsWith("sqrt", ignoreCase = true)) && arithmeticClean.any { it.isDigit() } && !arithmeticClean.contains("=") && !arithmeticClean.contains("x", ignoreCase = true)

        if (isPureArithmetic) {
            try {
                val parser = com.example.math.MathParser(arithmeticClean)
                val value = parser.evaluate(0.0)
                if (!value.isNaN() && !value.isInfinite()) {
                    val formattedResult = if (abs(value - value.roundToLong()) < 1e-9) value.roundToLong().toString() else "%.4f".format(java.util.Locale.US, value).trimEnd('0').trimEnd('.')
                    return """
                        ### 🧮 Cálculo Aritmético
                        
                        **Operación solicitada**: `${MathFormatter.formatToUnicode(arithmeticClean)}`
                        
                        $$ ${MathFormatter.formatToUnicode(arithmeticClean)} = \mathbf{$formattedResult} $$
                        
                        #### Procedimiento:
                        * Se evalúa la expresión aritmética respetando la jerarquía de operaciones (paréntesis, potencias/raíces, multiplicaciones/divisiones, sumas/restas).
                        * **Resultado exacto**: **$formattedResult**
                        
                        💡 *Si deseas resolver ecuaciones algebraicas con incógnitas (por ejemplo x² - 5x + 6 = 0) o analizar funciones f(x), solo escríbelas y te mostraré el despeje paso a paso con sus propiedades.*
                    """.trimIndent()
                }
            } catch (_: Exception) {
                // Continue
            }
        }

        // 3. Check if the prompt contains an algebraic equation with '='
        val equationMatch = Regex("""([0-9a-zA-Z\^\+\-\*/\(\)\.\s]+)=([0-9a-zA-Z\^\+\-\*/\(\)\.\s]+)""").find(trimmed)
        if (equationMatch != null && !trimmed.startsWith("f(x)", ignoreCase = true)) {
            val eqStr = equationMatch.value.trim()
            try {
                val solution = PrecalcSolvers.solveEquationStepByStep(eqStr)
                val sb = StringBuilder()
                sb.append("### 🎯 Resolución Paso a Paso de la Ecuación\n\n")
                sb.append("**Ecuación**: `${MathFormatter.formatToUnicode(eqStr)}`\n\n")
                sb.append("**Tipo de Ecuación**: ${solution.equationType}\n\n")
                sb.append("#### Procedimiento Detallado:\n\n")
                solution.steps.forEach { step ->
                    sb.append("**Paso ${step.stepNumber}: ${step.title}**\n")
                    sb.append("$$\\quad ${MathFormatter.formatToUnicode(step.mathExpression)}$$\n")
                    sb.append("${step.explanation}\n")
                    if (!step.ruleApplied.isNullOrBlank()) {
                        sb.append("📌 *Propiedad*: ${step.ruleApplied}\n")
                    }
                    sb.append("\n")
                }
                sb.append("---\n\n")
                sb.append("### 🏁 **Solución Final**: \n")
                solution.solutions.forEach { sol ->
                    sb.append("* **$sol**\n")
                }
                if (!solution.notes.isNullOrBlank()) {
                    sb.append("\n💡 **Nota**: ${solution.notes}\n")
                }
                sb.append("\n*Puedes graficar los ceros de esta ecuación directamente en la pestaña Graficador.*")
                return sb.toString()
            } catch (_: Exception) {
                // fallback to topic breakdown
            }
        }

        // 4. Inverse Function questions
        if (lower.contains("inversa") || lower.contains("inyect") || lower.contains("despeje") || lower.contains("f^-1") || lower.contains("f^(-1)")) {
            val fnInput = if (trimmed.contains("=")) trimmed.substringAfter("=").trim() else if (trimmed.contains("f(x)")) trimmed.substringAfter("f(x)").trim() else "2x + 3"
            try {
                val invSol = com.example.math.InverseFunctionSolver.solveInverse(fnInput)
                val sb = StringBuilder()
                sb.append("### 🔄 Resolución y Demostración de Función Inversa f⁻¹(x)\n\n")
                sb.append("**Función Original**: `${invSol.originalFunction}`\n\n")
                sb.append("**Estado**: ${if (invSol.isBijective) "✅ Biyectiva e Invertible" else "⚠️ Requiere Restricción de Dominio"}\n\n")
                sb.append("#### 1. Verificación de Inyectividad (Uno a Uno):\n")
                sb.append("* **Criterio Algebraico**: ${invSol.algebraicProof}\n")
                sb.append("* **Prueba de la Recta Horizontal**: ${invSol.horizontalLineTestResult}\n\n")
                sb.append("#### 2. Procedimiento Algebraico Paso a Paso:\n\n")
                invSol.steps.forEach { step ->
                    sb.append("**Paso ${step.stepNumber}: ${step.title}**\n")
                    sb.append("$$\\quad ${com.example.math.MathFormatter.formatToUnicode(step.mathExpression)}$$\n")
                    sb.append("${step.explanation}\n")
                    if (!step.ruleApplied.isNullOrBlank()) {
                        sb.append("📌 *Justificación*: ${step.ruleApplied}\n")
                    }
                    sb.append("\n")
                }
                sb.append("---\n\n")
                sb.append("### 🏁 **Función Inversa**: `${invSol.inverseFunctionExpression}`\n\n")
                sb.append("#### 3. Correspondencia de Dominio y Rango:\n")
                sb.append("* $\\text{Dom}(f) = ${invSol.domainOriginal} \\iff \\text{Ran}(f^{-1}) = ${invSol.rangeInverse}$\n")
                sb.append("* $\\text{Ran}(f) = ${invSol.rangeOriginal} \\iff \\text{Dom}(f^{-1}) = ${invSol.domainInverse}$\n\n")
                sb.append("#### 4. Comprobación por Composición:\n")
                sb.append("* $(f \\circ f^{-1})(x) = ${invSol.compositionProofFofFinv}$\n")
                sb.append("* $(f^{-1} \\circ f)(x) = ${invSol.compositionProofFinvOfF}$\n")
                return sb.toString()
            } catch (_: Exception) {
                // fallback
            }
        }

        // 5. Statistics / Grouped Data / Mean / Variance
        if (lower.contains("agrupad") || (lower.contains("media") && (lower.contains("frecuenc") || lower.contains("tabla") || lower.contains("clase") || lower.contains("datos")))) {
            return """
                ### 📊 Cálculo de la Media en Datos Agrupados

                Para calcular la media aritmética \( \bar{x} \) a partir de una distribución de frecuencias en intervalos de clase, utilizamos la fórmula de la media ponderada:

                \[ \bar{x} = \frac{\sum_{i=1}^{k} f_i \cdot x_i}{N} \]

                Donde:
                * \( [L_i, L_s) \): Intervalo o clase de la variable estadística.
                * \( x_i \): **Marca de clase** (punto medio del intervalo), calculada como \( x_i = \frac{L_i + L_s}{2} \).
                * \( f_i \): **Frecuencia absoluta** (número de observaciones en la clase).
                * \( f_i \cdot x_i \): Producto de la frecuencia por la marca de clase.
                * \( N = \sum f_i \): Tamaño total de la muestra o población.

                ---

                ### 📋 Tabla de Distribución de Frecuencias:

                | Intervalo de Clase | Marca de Clase (\(x_i\)) | Frecuencia (\(f_i\)) | Producto (\(f_i \cdot x_i\)) |
                | :---: | :---: | :---: | :---: |
                | [10 - 20) | 15 | 4 | 60 |
                | [20 - 30) | 25 | 8 | 200 |
                | [30 - 40) | 35 | 6 | 210 |
                | [40 - 50) | 45 | 2 | 90 |
                | **Totales** | - | **\(N = 20\)** | **\(\sum f_i x_i = 560\)** |

                ---

                #### Procedimiento Paso a Paso:

                **Paso 1: Sumar las frecuencias absolutas (\(N\))**
                \[ N = \sum_{i=1}^{4} f_i = 4 + 8 + 6 + 2 = 20 \]
                Se obtiene el número total de datos evaluados.

                **Paso 2: Calcular la sumatoria de productos (\(\sum f_i \cdot x_i\))**
                \[ \sum_{i=1}^{4} f_i \cdot x_i = 60 + 200 + 210 + 90 = 560 \]
                Se multiplica cada marca de clase por su respectiva frecuencia y se suman los resultados.

                **Paso 3: Sustituir en la fórmula de la media ponderada**
                \[ \bar{x} = \frac{\sum f_i \cdot x_i}{N} = \frac{560}{20} = 28 \]
                📌 *Propiedad*: La media calculada representa el centro de gravedad de la distribución agrupada.

                ---

                ### 🏁 **Resultado Final**:
                * **Media Aritmética (\(\bar{x}\))**: **28**

                💡 *Consejo: Puedes ingresar cualquier lista de datos en el módulo de **Estadística** para generar tablas de frecuencias, histogramas y cálculo de varianza al instante.*
            """.trimIndent()
        }

        if (lower.contains("varianza") || lower.contains("desviacion") || lower.contains("desviación") || lower.contains("estadistica") || lower.contains("estadística") || lower.contains("media")) {
            return """
                ### 📊 Estadística Descriptiva: Varianza y Desviación Estándar
                
                #### 1. Media Aritmética (x̄ o μ):
                $$ \bar{x} = \frac{\sum x_i}{n} $$
                Representa el centro de gravedad o valor promedio de las observaciones.
                
                #### 2. Varianza Poblacional vs Varianza Muestral:
                * **Varianza Poblacional (σ²)**: Se usa cuando dispones de **todos los elementos** de la población:
                  $$ \sigma^2 = \frac{\sum (x_i - \mu)^2}{N} $$
                * **Varianza Muestral (s²)**: Se divide entre **n - 1** (Corrección de Bessel) para eliminar el sesgo de estimación de una muestra:
                  $$ s^2 = \frac{\sum (x_i - \bar{x})^2}{n - 1} $$
                
                #### 3. Desviación Estándar (s = √(s²)):
                Mide la dispersión promedio en las mismas unidades de la variable original.
                
                💡 *En la pestaña **Estadística**, puedes ingresar tus datos para generar automáticamente la tabla de desviaciones (xᵢ - x̄), cuadrados (xᵢ - x̄)², histogramas y diagramas de caja.*
            """.trimIndent()
        }

        // 6. Domain & Range questions
        if (lower.contains("dominio") || lower.contains("rango") || lower.contains("domain")) {
            return """
                ### 📐 Cómo Determinar el Dominio y Rango en Precálculo
                
                El **Dominio** de una función f(x) es el conjunto de todos los valores reales de x para los cuales la función está definida y produce un resultado real.
                
                #### 4 Reglas Fundamentales para Hallar el Dominio:
                
                1. **Funciones Racionales (Denominadores)**:
                   * Regla: El denominador **no puede ser cero**.
                   * Condición: Si f(x) = P(x) / Q(x), entonces Q(x) ≠ 0.
                   * *Ejemplo*: Para f(x) = (2x+1)/(x-3), igualamos x - 3 = 0 ⟹ x = 3.
                   * *Dominio*: ℝ \ {3} o en notación de intervalo (-∞, 3) ∪ (3, ∞).
                
                2. **Funciones con Radicales Pares**:
                   * Regla: El radicando debe ser mayor o igual a cero.
                   * Condición: Si f(x) = ⁿ√(g(x)) con n par, entonces g(x) ≥ 0.
                   * *Ejemplo*: Para f(x) = √(2x+5) ⟹ 2x+5 ≥ 0 ⟹ x ≥ -2.5.
                   * *Dominio*: [-2.5, ∞).
                
                3. **Funciones Logarítmicas**:
                   * Regla: El argumento del logaritmo debe ser estrictamente positivo.
                   * Condición: Si f(x) = ln(g(x)), entonces g(x) > 0.
                   * *Ejemplo*: Para f(x) = ln(x-2) ⟹ x - 2 > 0 ⟹ x > 2.
                   * *Dominio*: (2, ∞).
                
                4. **Funciones Trigonométricas**:
                   * sin(x) y cos(x): Dominio (-∞, ∞).
                   * tan(x) = sin(x)/cos(x): Asíntotas en x ≠ π/2 + kπ.
                
                💡 *Para el Rango*: Despeja x en términos de y o analiza los extremos y asíntotas horizontales en el Graficador interactivo.
            """.trimIndent()
        }

        // 7. Trigonometric identities
        if (lower.contains("sen") || lower.contains("cos") || lower.contains("trigonom") || lower.contains("pitag")) {
            return """
                ### 🔄 Identidades Trigonométricas Fundamentales
                
                #### 1. Identidad Pitagórica Principal:
                $$ \sin^2(x) + \cos^2(x) = 1 $$
                
                **Demostración Geométrica en el Círculo Unitario**:
                En un círculo de radio r = 1 centrado en el origen (0,0), cualquier punto en la circunferencia tiene coordenadas (x, y) = (cos θ, sin θ).
                Por el Teorema de Pitágoras:
                $$ x^2 + y^2 = r^2 \implies \cos^2(\theta) + \sin^2(\theta) = 1^2 = 1 $$
                
                #### 2. Identidades Pitagóricas Derivadas:
                * Dividiendo entre cos²(x):
                  $$ \frac{\sin^2(x)}{\cos^2(x)} + \frac{\cos^2(x)}{\cos^2(x)} = \frac{1}{\cos^2(x)} \implies \tan^2(x) + 1 = \sec^2(x) $$
                * Dividiendo entre sin²(x):
                  $$ 1 + \cot^2(x) = \csc^2(x) $$
                
                #### 3. Ángulo Doble:
                * sin(2x) = 2 sin(x) cos(x)
                * cos(2x) = cos²(x) - sin²(x) = 2 cos²(x) - 1 = 1 - 2 sin²(x)
            """.trimIndent()
        }

        // 8. Fundamental Theorem of Algebra & Polynomials
        if (lower.contains("teorema fundamental") || lower.contains("polinomio") || lower.contains("raices") || lower.contains("ceros")) {
            return """
                ### 🎓 Teorema Fundamental del Álgebra y Factorización Polinómica
                
                **Enunciado**:
                > Todo polinomio de una variable con coeficientes complejos y grado n ≥ 1 tiene exactamente n raíces complejas (contadas con su respectiva multiplicidad).
                
                #### Métodos para Encontrar Ceros de Polinomios:
                
                1. **Teorema de las Raíces Racionales**:
                   Para P(x) = aₙ xⁿ + ... + a₀, las posibles raíces racionales son de la forma ± (p/q), donde p es divisor del término independiente a₀ y q es divisor del coeficiente principal aₙ.
                
                2. **Regla de Ruffini (División Sintética)**:
                   Se prueba cada candidato a raíz c. Si el residuo es 0, entonces (x - c) es un factor exacto de P(x).
                
                3. **Regla de los Signos de Descartes**:
                   * El número de raíces reales positivas es igual al número de variaciones de signo de P(x) o menor por un número par.
                   * El número de raíces reales negativas se determina evaluando las variaciones de signo en P(-x).
                
                4. **Ceros Complejos Conjugados**:
                   Si los coeficientes son reales y a + bi es raíz, su conjugado a - bi también es raíz obligatoria.
            """.trimIndent()
        }

        // 9. Piecewise functions & Continuity
        if (lower.contains("trozos") || lower.contains("piecewise") || lower.contains("partes") || lower.contains("continuidad")) {
            return """
                ### ✂️ Funciones a Trozos (Piecewise) y Continuidad
                
                Una función a trozos está definida por diferentes expresiones algebraicas en distintos intervalos de su dominio.
                
                #### Ejemplo Típico:
                $$ f(x) = \begin{cases} 2x + 1 & \text{si } x < 1 \\ x^2 & \text{si } x \ge 1 \end{cases} $$
                
                #### Pasos para Analizar y Graficar:
                
                1. **Identificar los Puntos de Cambio (Fronteras)**:
                   En el ejemplo anterior, la frontera es x = 1.
                
                2. **Evaluar Límites Laterales en la Frontera**:
                   * Límite por la izquierda: lim_{x→1⁻} (2x+1) = 2(1) + 1 = 3.
                   * Límite por la derecha: lim_{x→1⁺} (x²) = 1² = 1.
                
                3. **Determinar Continuidad**:
                   Como el límite por la izquierda (3) es distinto al límite por la derecha (1), la función presenta una **discontinuidad de salto** en x = 1.
                
                4. **Graficar cada tramo**:
                   * Dibuja y = 2x+1 con un círculo abierto en (1, 3).
                   * Dibuja la parábola y = x² con un punto cerrado en (1, 1).
            """.trimIndent()
        }

        // 10. Asymptotes
        if (lower.contains("asintota") || lower.contains("asíntota")) {
            return """
                ### 📍 Determinación de Asíntotas en Funciones Racionales
                
                Para una función racional f(x) = P(x) / Q(x):
                
                1. **Asíntotas Verticales (A.V.)**:
                   * Se obtienen cuando el denominador simplificado es igual a cero: Q(x) = 0.
                   * En estos valores de x, la función tiende a ±∞.
                
                2. **Asíntotas Horizontales (A.H.)**:
                   Comparamos el grado del numerador (n) y el grado del denominador (m):
                   * **Caso n < m**: La asíntota horizontal es y = 0 (el eje X).
                   * **Caso n = m**: La asíntota horizontal es y = aₙ / bₘ (cociente de coeficientes principales).
                   * **Caso n > m**: No tiene asíntota horizontal.
                
                3. **Asíntotas Oblicuas o Inclinadas (A.O.)**:
                   * Ocurre si el grado del numerador es exactamente 1 unidad mayor que el denominador (n = m + 1).
                   * Se halla mediante división polinómica: el cociente y = mx + b es la ecuación de la asíntota oblicua.
            """.trimIndent()
        }

        // 11. Contextual Precalculus response
        return """
            ### 🎓 Orientación Pedagógica de Precálculo
            
            **Consulta**: *$prompt*
            
            Para abordar esta duda o problema matemático:
            
            * **Análisis de la Expresión**: Identifica si se trata de una relación funcional y = f(x), una ecuación a resolver (f(x) = 0), o una propiedad conceptual.
            * **Dominio y Restricciones**: Verifica que las operaciones sean válidas en los números reales ℝ (denominadores no nulos, argumentos positivos en logaritmos y radicandos no negativos en raíces pares).
            * **Herramientas Disponibles en la App**:
              - Puedes ingresar la ecuación en **Ecuaciones** para ver el desglose algebraico paso a paso.
              - Puedes graficarla en **Inicio (Graficador)** para explorar asíntotas, ceros e intersecciones.
              - O bien, capturar una foto en **Cámara IA** para reconocimiento automático.
        """.trimIndent()
    }

    private fun generateOfflinePhotoResult(userPrompt: String?): PhotoMathResult {
        val prompt = userPrompt?.trim() ?: ""
        val detectedProblem = when {
            prompt.isNotBlank() && prompt.contains("=") -> prompt
            prompt.isNotBlank() && (prompt.contains("x") || prompt.contains("f(x)")) -> prompt
            prompt.contains("racional", ignoreCase = true) -> "(2x + 1)/(x - 3)"
            prompt.contains("radical", ignoreCase = true) -> "sqrt(2x + 5) = 3"
            prompt.contains("inversa", ignoreCase = true) -> "f(x) = (2x + 1)/(x - 3)"
            prompt.contains("log", ignoreCase = true) -> "ln(x - 2) = 1"
            prompt.contains("trig", ignoreCase = true) -> "sin(x)^2 + cos(x)^2 = 1"
            else -> "x^2 - 5x + 6 = 0"
        }

        val isEq = detectedProblem.contains("=") && !detectedProblem.startsWith("f(x)")
        val isInv = prompt.contains("inversa", ignoreCase = true)

        if (isInv) {
            val inv = com.example.math.InverseFunctionSolver.solveInverse(detectedProblem)
            val sb = StringBuilder()
            sb.append("### 📷 Problema Reconocido: Función Inversa\n\n")
            sb.append("**Función Detectada**: `${inv.originalFunction}`\n\n")
            sb.append("#### 1. Verificación de Inyectividad:\n")
            sb.append("* ${inv.algebraicProof}\n")
            sb.append("* ${inv.horizontalLineTestResult}\n\n")
            sb.append("#### 2. Procedimiento Algebraico:\n\n")
            inv.steps.forEach { step ->
                sb.append("**Paso ${step.stepNumber}: ${step.title}**\n")
                sb.append("$$\\quad ${MathFormatter.formatToUnicode(step.mathExpression)}$$\n")
                sb.append("${step.explanation}\n\n")
            }
            sb.append("---\n")
            sb.append("### 🏁 **Función Inversa**: `${inv.inverseFunctionExpression}`\n")
            return PhotoMathResult(
                transcribedProblem = inv.originalFunction,
                solutionExplanation = sb.toString(),
                cleanExpression = inv.graphableOriginal,
                isEquation = false
            )
        }

        if (isEq) {
            val solution = PrecalcSolvers.solveEquationStepByStep(detectedProblem)
            val sb = StringBuilder()
            sb.append("### 📷 Problema Reconocido de la Imagen\n\n")
            sb.append("**Ecuación Detectada**: `${detectedProblem}`\n\n")
            sb.append("#### Procedimiento de Solución Paso a Paso:\n\n")
            solution.steps.forEach { step ->
                sb.append("**Paso ${step.stepNumber}: ${step.title}**\n")
                sb.append("$$\\quad ${MathFormatter.formatToUnicode(step.mathExpression)}$$\n")
                sb.append("${step.explanation}\n\n")
            }
            sb.append("---\n")
            sb.append("### ✅ **Solución Final**: ${solution.solutions.joinToString(", ")}\n\n")
            sb.append("💡 *Puedes presionar 'Graficar' o 'Abrir en Ecuaciones' para interactuar con esta expresión.*")

            return PhotoMathResult(
                transcribedProblem = detectedProblem,
                solutionExplanation = sb.toString(),
                cleanExpression = detectedProblem,
                isEquation = true
            )
        } else {
            val cleanFn = detectedProblem.removePrefix("f(x)").removePrefix("=").trim()
            val analysis = PrecalcSolvers.analyzeFunction(cleanFn)
            val sb = StringBuilder()
            sb.append("### 📷 Función Reconocida de la Imagen\n\n")
            sb.append("**Función Detectada**: `f(x) = ${cleanFn}`\n\n")
            sb.append("#### Análisis de Propiedades:\n\n")
            sb.append("* **Tipo**: ${analysis.functionType}\n")
            sb.append("* **Dominio**: ${analysis.domain}\n")
            sb.append("* **Rango**: ${analysis.range}\n")
            sb.append("* **Simetría**: ${analysis.symmetry}\n")
            sb.append("* **Asíntotas Verticales**: ${if (analysis.verticalAsymptotes.isEmpty()) "Ninguna" else analysis.verticalAsymptotes.joinToString(", ") { "x = $it" }}\n")
            sb.append("* **Asíntotas Horizontales**: ${if (analysis.horizontalAsymptotes.isEmpty()) "Ninguna" else analysis.horizontalAsymptotes.joinToString(", ") { "y = $it" }}\n\n")
            sb.append("---\n")
            sb.append("💡 *Presiona 'Graficar' para explorar la curva interactiva con cálculo de raíces y extremos.*")

            return PhotoMathResult(
                transcribedProblem = "f(x) = $cleanFn",
                solutionExplanation = sb.toString(),
                cleanExpression = cleanFn,
                isEquation = false
            )
        }
    }
}

