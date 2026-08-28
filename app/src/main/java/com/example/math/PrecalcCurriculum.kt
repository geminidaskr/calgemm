package com.example.math

/**
 * Complete structured Precalculus curriculum database with explanations, formulas, and worked examples.
 */
object PrecalcCurriculum {

    val topics: List<PrecalcTopic> = listOf(
        PrecalcTopic(
            id = "functions_basics",
            title = "Funciones y Sus Gráficas",
            subtitle = "Dominio, Rango, Simetría y Tasa de Cambio",
            category = TopicCategory.FUNCTIONS_AND_GRAPHS,
            iconName = "timeline",
            summary = "Fundamentos de una función: regla de correspondencia donde a cada elemento del dominio le corresponde un único elemento del rango.",
            theoryMarkdown = """
                ### ¿Qué es una Función?
                Una función **f** de un conjunto A en un conjunto B es una regla que asigna a cada elemento **x ∈ A** exactamente un único elemento **f(x) ∈ B**.
                
                ### Prueba de la Recta Vertical
                Una curva en el plano cartesiano representa la gráfica de una función si y solo si ninguna recta vertical corta la gráfica en más de un punto.
                
                ### Dominio y Rango
                * **Dominio**: Conjunto de todos los valores de entrada 'x' para los cuales f(x) está definida en los reales.
                * **Rango**: Conjunto de todos los valores de salida 'y = f(x)' posibles.
                
                ### Simetría de Funciones
                * **Función Par**: f(-x) = f(x). Es simétrica con respecto al eje Y (ejemplo: f(x) = x²).
                * **Función Impar**: f(-x) = -f(x). Es simétrica con respecto al origen (0,0) (ejemplo: f(x) = x³).
            """.trimIndent(),
            keyFormulas = listOf(
                KeyFormula("Definición de Función Par", "f(-x) = f(x)", "Simetría respecto al eje Y"),
                KeyFormula("Definición de Función Impar", "f(-x) = -f(x)", "Simetría respecto al origen"),
                KeyFormula("Tasa de Cambio Promedio", "(f(b) - f(a)) / (b - a)", "Pendiente de la recta secante entre x=a y x=b")
            ),
            examples = listOf(
                TopicExample(
                    title = "Dominio de una función racional",
                    problem = "Determinar el dominio de f(x) = (2x + 1) / (x² - 9)",
                    solutionSteps = listOf(
                        SolutionStep(1, "Identificar restricciones en el denominador", "x² - 9 ≠ 0", "El denominador de una fracción nunca puede ser cero."),
                        SolutionStep(2, "Factorizar la diferencia de cuadrados", "(x - 3)(x + 3) ≠ 0", "Factorizamos como diferencia de cuadrados."),
                        SolutionStep(3, "Hallar los valores excluidos", "x ≠ 3  y  x ≠ -3", "Los valores 3 y -3 anulan el denominador.")
                    ),
                    finalAnswer = "Dom(f) = ℝ \\ {-3, 3} = (-∞, -3) ∪ (-3, 3) ∪ (3, +∞)",
                    graphableFunction = "(2x + 1)/(x^2 - 9)"
                ),
                TopicExample(
                    title = "Comprobar si f(x) es Par o Impar",
                    problem = "Determinar si f(x) = x⁴ - 3x² + 2 es par, impar o ninguna.",
                    solutionSteps = listOf(
                        SolutionStep(1, "Evaluar f(-x)", "f(-x) = (-x)⁴ - 3(-x)² + 2", "Sustituimos x por (-x)."),
                        SolutionStep(2, "Simplificar potencias", "f(-x) = x⁴ - 3x² + 2", "Las potencias pares eliminan el signo negativo."),
                        SolutionStep(3, "Comparar con f(x)", "f(-x) = f(x)", "Coincide exactamente con la función original.")
                    ),
                    finalAnswer = "La función es PAR (simétrica respecto al eje Y).",
                    graphableFunction = "x^4 - 3x^2 + 2"
                )
            )
        ),
        PrecalcTopic(
            id = "polynomial_rational",
            title = "Polinomios y Funciones Racionales",
            subtitle = "Raíces, Factorización, Teorema del Resto y Asíntotas",
            category = TopicCategory.POLYNOMIAL_AND_RATIONAL,
            iconName = "superscript",
            summary = "Estudio exhaustivo de polinomios de grado n, raíces reales/complejas, teorema fundamental del álgebra y asíntotas racionales.",
            theoryMarkdown = """
                ### Forma Estándar de la Cuadrática
                La función cuadrática f(x) = ax² + bx + c se puede escribir en forma vértice:
                **f(x) = a(x - h)² + k**
                donde el vértice es el punto **(h, k)** con **h = -b / (2a)** y **k = f(h)**.
                
                ### Asíntotas de Funciones Racionales R(x) = P(x) / Q(x)
                1. **Asíntotas Verticales**: Rectas x = c donde Q(c) = 0 y P(c) ≠ 0.
                2. **Asíntotas Horizontales**:
                   * Si grado(P) < grado(Q) ⇒ y = 0.
                   * Si grado(P) = grado(Q) ⇒ y = a_n / b_n (cociente de coeficientes líderes).
                   * Si grado(P) > grado(Q) ⇒ No hay asíntota horizontal (posible asíntota oblicua si grado(P) = grado(Q) + 1).
            """.trimIndent(),
            keyFormulas = listOf(
                KeyFormula("Fórmula Cuadrática", "x = (-b ± √(b² - 4ac)) / (2a)", "Raíces de ax² + bx + c = 0"),
                KeyFormula("Coordenada X del Vértice", "h = -b / (2a)", "Eje de simetría de la parábola"),
                KeyFormula("Discriminante", "Δ = b² - 4ac", "Determina el número y tipo de raíces")
            ),
            examples = listOf(
                TopicExample(
                    title = "Asíntotas de una función racional",
                    problem = "Encontrar las asíntotas de f(x) = (3x² - 6x) / (x² - 4)",
                    solutionSteps = listOf(
                        SolutionStep(1, "Factorizar numerador y denominador", "f(x) = (3x(x - 2)) / ((x - 2)(x + 2))", "Factor común y diferencia de cuadrados."),
                        SolutionStep(2, "Simplificar factores comunes", "f(x) = 3x / (x + 2)  con x ≠ 2 (agujero en x=2)", "El factor (x-2) genera un punto de discontinuidad removible."),
                        SolutionStep(3, "Hallar asíntota vertical", "x + 2 = 0  ⇒  x = -2", "Asíntota vertical en x = -2."),
                        SolutionStep(4, "Hallar asíntota horizontal", "Grados iguales (2 y 2) ⇒ y = 3/1 = 3", "Asíntota horizontal en y = 3.")
                    ),
                    finalAnswer = "Asíntota vertical: x = -2 | Asíntota horizontal: y = 3 | Agujero en x = 2",
                    graphableFunction = "(3x^2 - 6x)/(x^2 - 4)"
                )
            )
        ),
        PrecalcTopic(
            id = "exponential_logarithmic",
            title = "Funciones Exponenciales y Logarítmicas",
            subtitle = "Número e, Crecimiento/Decaimiento y Leyes de Logaritmos",
            category = TopicCategory.EXPONENTIAL_AND_LOGARITHMIC,
            iconName = "show_chart",
            summary = "Comportamiento del crecimiento continuo, función exponencial natural e^x y su inversa el logaritmo natural ln(x).",
            theoryMarkdown = """
                ### Definición de Logaritmo
                **y = log_b(x) ⟺ b^y = x** (con b > 0, b ≠ 1 y x > 0).
                
                ### Leyes Fundamentales de los Logaritmos
                1. **Producto**: log_b(u · v) = log_b(u) + log_b(v)
                2. **Cociente**: log_b(u / v) = log_b(u) - log_b(v)
                3. **Potencia**: log_b(u^k) = k · log_b(u)
                4. **Cambio de Base**: log_b(x) = ln(x) / ln(b) = log(x) / log(b)
                
                ### Interés Compuesto Continuo y Crecimiento
                **A(t) = P · e^(rt)**
            """.trimIndent(),
            keyFormulas = listOf(
                KeyFormula("Definición Fundamental", "log_b(x) = y ⟺ bʸ = x", "Equivalencia logarítmica y exponencial"),
                KeyFormula("Ley de la Potencia", "ln(xⁿ) = n · ln(x)", "Permite bajar exponentes al resolver"),
                KeyFormula("Cambio de Base", "log_b(x) = ln(x) / ln(b)", "Conversión a logaritmo natural")
            ),
            examples = listOf(
                TopicExample(
                    title = "Resolver ecuación exponencial",
                    problem = "Resolver: 4 · e^(2x) - 12 = 0",
                    solutionSteps = listOf(
                        SolutionStep(1, "Aislar el término exponencial", "4 · e^(2x) = 12  ⇒  e^(2x) = 3", "Sumamos 12 y dividimos entre 4."),
                        SolutionStep(2, "Aplicar logaritmo natural a ambos lados", "ln(e^(2x)) = ln(3)", "El logaritmo natural es la función inversa de e."),
                        SolutionStep(3, "Simplificar el exponente", "2x = ln(3)", "ln(e^u) = u."),
                        SolutionStep(4, "Despejar x", "x = ln(3) / 2 ≈ 1.0986 / 2 ≈ 0.5493", "Dividimos entre 2.")
                    ),
                    finalAnswer = "x = ln(3)/2 ≈ 0.5493",
                    graphableFunction = "4*e^(2x) - 12"
                )
            )
        ),
        PrecalcTopic(
            id = "trigonometry",
            title = "Trigonometría y Círculo Unitario",
            subtitle = "Razones Trigonométricas, Identidades y Gráficas de Ondas",
            category = TopicCategory.TRIGONOMETRY,
            iconName = "pie_chart",
            summary = "El círculo unitario (radio=1), medidas en radianes, 6 funciones trigonométricas y sus identidades fundamentales.",
            theoryMarkdown = """
                ### Círculo Unitario (x² + y² = 1)
                Para cualquier ángulo θ:
                * **cos(θ) = x** (Coordenada X)
                * **sen(θ) = y** (Coordenada Y)
                * **tan(θ) = y / x = sen(θ) / cos(θ)**
                
                ### Identidades Pitagóricas
                * **sen²(θ) + cos²(θ) = 1**
                * **1 + tan²(θ) = sec²(θ)**
                * **1 + cot²(θ) = csc²(θ)**
                
                ### Parámetros de la Onda Senoidal y Cosenoidal: y = A · sen(Bx - C) + D
                * **Amplitud**: |A|
                * **Período**: T = 2π / |B|
                * **Desfase (Desplazamiento horizontal)**: C / B
                * **Desplazamiento vertical**: D
            """.trimIndent(),
            keyFormulas = listOf(
                KeyFormula("Identidad Pitagórica", "sen²(θ) + cos²(θ) = 1", "Identidad fundamental de la trigonometría"),
                KeyFormula("Período de Seno y Coseno", "T = 2π / |B|", "Longitud de un ciclo completo"),
                KeyFormula("Ángulo Doble", "sen(2θ) = 2 sen(θ) cos(θ)", "Fórmula de ángulo doble para seno")
            ),
            examples = listOf(
                TopicExample(
                    title = "Análisis de una función senoidal",
                    problem = "Determinar amplitud, período y gráfica de f(x) = 3 · sen(2x - π)",
                    solutionSteps = listOf(
                        SolutionStep(1, "Identificar Amplitud", "|A| = |3| = 3", "La amplitud es el valor absoluto del coeficiente líder."),
                        SolutionStep(2, "Calcular el Período T", "T = 2π / B = 2π / 2 = π", "El período se comprime a π radianes."),
                        SolutionStep(3, "Calcular el Desfase", "Desfase = C / B = π / 2", "Desplazamiento de π/2 unidades a la derecha.")
                    ),
                    finalAnswer = "Amplitud = 3 | Período = π | Desfase = π/2 hacia la derecha",
                    graphableFunction = "3*sin(2x - pi)"
                )
            )
        ),
        PrecalcTopic(
            id = "transformations",
            title = "Transformaciones y Composición de Funciones",
            subtitle = "Desplazamientos, Reflexiones, (f ∘ g)(x) y Función Inversa f⁻¹(x)",
            category = TopicCategory.TRANSFORMATIONS_AND_COMPOSITION,
            iconName = "transform",
            summary = "Efecto de parámetros en f(x): traslaciones, escalamientos y reflexión, junto al álgebra de composición e inversión.",
            theoryMarkdown = """
                ### Reglas de Transformación para y = a · f(b(x - h)) + k
                * **h > 0**: Desplazamiento horizontal a la derecha **h** unidades.
                * **k > 0**: Desplazamiento vertical hacia arriba **k** unidades.
                * **a < 0**: Reflexión respecto al eje X.
                * **b < 0**: Reflexión respecto al eje Y.
                * **|a| > 1**: Alargamiento vertical; **0 < |a| < 1**: Compresión vertical.
                
                ### Composición de Funciones
                **(f ∘ g)(x) = f(g(x))**
                El dominio de (f ∘ g) son todos los x en el dominio de g tales que g(x) está en el dominio de f.
                
                ### Función Inversa f⁻¹(x)
                f tiene inversa si y solo si es **uno a uno (inyectiva)** (pasa la prueba de la recta horizontal).
                Propiedad: **f(f⁻¹(x)) = x** y **f⁻¹(f(x)) = x**.
            """.trimIndent(),
            keyFormulas = listOf(
                KeyFormula("Composición de Funciones", "(f ∘ g)(x) = f(g(x))", "Evaluación de f en la salida de g"),
                KeyFormula("Propiedad Inversa", "f(f⁻¹(x)) = x", "Identidad funcional"),
                KeyFormula("Traslación General", "y = f(x - h) + k", "Vértice/Centro desplazado a (h, k)")
            ),
            examples = listOf(
                TopicExample(
                    title = "Hallar la función inversa f⁻¹(x)",
                    problem = "Encontrar la inversa de f(x) = (2x + 5) / (x - 3)",
                    solutionSteps = listOf(
                        SolutionStep(1, "Escribir como y = f(x)", "y = (2x + 5) / (x - 3)", "Planteamos la igualdad con y."),
                        SolutionStep(2, "Intercambiar variables x e y", "x = (2y + 5) / (y - 3)", "Intercambio x ↔ y."),
                        SolutionStep(3, "Multiplicar por el denominador", "x(y - 3) = 2y + 5  ⇒  xy - 3x = 2y + 5", "Distribuimos."),
                        SolutionStep(4, "Agrupar términos con 'y'", "xy - 2y = 3x + 5  ⇒  y(x - 2) = 3x + 5", "Factor común y."),
                        SolutionStep(5, "Despejar y", "y = (3x + 5) / (x - 2)", "Dividimos entre (x - 2).")
                    ),
                    finalAnswer = "f⁻¹(x) = (3x + 5) / (x - 2) con x ≠ 2",
                    graphableFunction = "(3x + 5)/(x - 2)"
                )
            )
        ),
        PrecalcTopic(
            id = "statistics_basics",
            title = "Estadística Descriptiva y Gráficos",
            subtitle = "Medidas Centrales, Dispersión, Frecuencias y Gráficos",
            category = TopicCategory.STATISTICS_AND_PROBABILITY,
            iconName = "analytics",
            summary = "Fundamentos del análisis de datos: media, mediana, moda, varianza, desviación estándar, regla de Sturges y recomendación gráfica.",
            theoryMarkdown = """
                ### Medidas de Tendencia Central
                * **Media Aritmética (x̄)**: Suma de todos los datos dividida entre el tamaño muestral n: **x̄ = Σx / n**.
                * **Mediana (Me)**: Valor que divide la muestra ordenada en dos partes iguales (percentil 50).
                * **Moda (Mo)**: Valor o valores que ocurren con mayor frecuencia absoluta.
                
                ### Medidas de Dispersión
                * **Varianza Muestral (s²)**: **s² = Σ(x - x̄)² / (n - 1)**.
                * **Desviación Estándar (s)**: Raíz cuadrada de la varianza: **s = √s²**.
                * **Coeficiente de Variación (CV)**: Grado de homogeneidad: **CV = (s / |x̄|) · 100%**.
                
                ### Tabla de Frecuencias & Regla de Sturges
                Para datos cuantitativos continuos, el número óptimo de clases o intervalos es **k = 1 + 3.322 · log₁₀(n)** y la amplitud del intervalo es **A = (Max - Min) / k**.
                
                ### Selección del Gráfico Adecuado
                * **Histograma y Polígono de Frecuencias**: Para variables cuantitativas continuas agrupadas en intervalos.
                * **Diagrama de Barras**: Para variables cualitativas o cuantitativas discretas con pocas categorías.
                * **Gráfico Circular (Sectores)**: Para representar proporciones del 100% con 6 categorías o menos.
                * **Diagrama de Dispersión**: Para pares de datos (x, y) que buscan estudiar correlación y regresión lineal.
                * **Diagrama de Caja y Bigotes (Box Plot)**: Para visualizar cuartiles (Q1, Mediana, Q3) y valores atípicos (outliers).
            """.trimIndent(),
            keyFormulas = listOf(
                KeyFormula("Media Muestral", "x̄ = (Σ x_i) / n", "Promedio aritmético de los datos"),
                KeyFormula("Desviación Estándar", "s = √[ Σ(x_i - x̄)² / (n - 1) ]", "Medida de dispersión muestral"),
                KeyFormula("Regla de Sturges", "k = 1 + 3.322 · log₁₀(n)", "Número recomendado de intervalos"),
                KeyFormula("Regresión Lineal", "y = mx + b ; r = S_xy / (s_x · s_y)", "Ajuste de mínimos cuadrados y correlación")
            ),
            examples = listOf(
                TopicExample(
                    title = "Cálculo de Media, Mediana y Varianza",
                    problem = "Dada la muestra: 12, 15, 14, 18, 16, calcular x̄, Me y s².",
                    solutionSteps = listOf(
                        SolutionStep(1, "Ordenar los datos", "12, 14, 15, 16, 18 (n = 5)", "Orden ascendente."),
                        SolutionStep(2, "Calcular la Media", "x̄ = (12 + 14 + 15 + 16 + 18) / 5 = 75 / 5 = 15", "Suma dividida entre 5."),
                        SolutionStep(3, "Hallar la Mediana", "Me = posición (5 + 1)/2 = 3° valor = 15", "Elemento central."),
                        SolutionStep(4, "Calcular Desviaciones al Cuadrado", "(12-15)² + (14-15)² + (15-15)² + (16-15)² + (18-15)² = 9 + 1 + 0 + 1 + 9 = 20", "Σ(x - x̄)²."),
                        SolutionStep(5, "Calcular Varianza Muestral", "s² = 20 / (5 - 1) = 20 / 4 = 5.0", "Dividir entre n - 1.")
                    ),
                    finalAnswer = "x̄ = 15.0,  Me = 15.0,  s² = 5.0,  s = 2.236"
                )
            )
        )
    )
}
