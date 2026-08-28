package com.example.math

/**
 * Parses and formats math expressions into clean Unicode and structured tokens
 * for native mathematical rendering.
 */
object MathFormatter {

    private val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ', 'k' to 'ᵏ', 't' to 'ᵗ'
    )

    private val subscriptMap = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'o' to 'ₒ', 'x' to 'ₓ', 'h' to 'ₕ', 'k' to 'ₖ',
        'i' to 'ᵢ', 'j' to 'ⱼ', 'n' to 'ₙ', 'm' to 'ₘ', 'p' to 'ₚ', 'r' to 'ᵣ', 't' to 'ₜ', 'u' to 'ᵤ', 'v' to 'ᵥ'
    )

    /**
     * Converts a standard raw mathematical string into beautiful Unicode typography.
     * Replaces sqrt(...) with √( ... ), ^2 with ², ^(x+1) with ⁽ˣ⁺¹⁾, log_2(x) with log₂(x), etc.
     */
    fun formatToUnicode(rawExpr: String): String {
        // If the string contains LaTeX markers, run full LaTeX parsing
        if (rawExpr.contains("\\") || rawExpr.contains("^{") || rawExpr.contains("_{")) {
            return latexToUnicode(rawExpr)
        }

        var s = rawExpr.trim()
            .replace("*", " · ")
            .replace("pi", "π")
            .replace("PI", "π")
            .replace("theta", "θ")
            .replace("infinity", "∞")
            .replace("inf", "∞")
            .replace("<=", "≤")
            .replace(">=", "≥")
            .replace("!=", "≠")
            .replace("+-", "±")

        // Replace sqrt(arg) with √(arg)
        s = replaceFunctionCalls(s, "sqrt", "√")
        s = replaceFunctionCalls(s, "cbrt", "∛")

        // Replace exponents: x^2 -> x², x^(2x+1) -> x⁽²ˣ⁺¹⁾
        s = formatExponents(s)

        // Replace subscripts: log_2 -> log₂
        s = formatSubscripts(s)

        return s
    }

    private fun replaceFunctionCalls(text: String, fnName: String, replacementPrefix: String): String {
        var result = text
        while (true) {
            val idx = result.indexOf("$fnName(")
            if (idx == -1) break
            val openParenIdx = idx + fnName.length
            val closeParenIdx = findMatchingParen(result, openParenIdx)
            if (closeParenIdx == -1) break

            val inner = result.substring(openParenIdx + 1, closeParenIdx)
            val formattedInner = formatToUnicode(inner)
            val replaced = "$replacementPrefix($formattedInner)"
            result = result.substring(0, idx) + replaced + result.substring(closeParenIdx + 1)
        }
        return result
    }

    private fun findMatchingParen(text: String, openIdx: Int): Int {
        var depth = 0
        for (i in openIdx until text.length) {
            if (text[i] == '(') depth++
            else if (text[i] == ')') {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }

    private fun formatExponents(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            if (text[i] == '^') {
                i++
                if (i < text.length && text[i] == '(') {
                    val openParen = i
                    val closeParen = findMatchingParen(text, openParen)
                    if (closeParen != -1) {
                        val expContent = text.substring(openParen + 1, closeParen)
                        for (ch in expContent) {
                            sb.append(superscriptMap[ch] ?: ch)
                        }
                        i = closeParen + 1
                    } else {
                        sb.append('^')
                    }
                } else {
                    // Single char or number
                    val start = i
                    while (i < text.length && (text[i].isDigit() || text[i].isLetter())) {
                        val ch = text[i]
                        sb.append(superscriptMap[ch] ?: ch)
                        i++
                    }
                    if (start == i && i < text.length) {
                        sb.append(superscriptMap[text[i]] ?: text[i])
                        i++
                    }
                }
            } else {
                sb.append(text[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun formatSubscripts(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            if (text[i] == '_') {
                i++
                if (i < text.length && text[i] == '(') {
                    val openParen = i
                    val closeParen = findMatchingParen(text, openParen)
                    if (closeParen != -1) {
                        val subContent = text.substring(openParen + 1, closeParen)
                        for (ch in subContent) {
                            sb.append(subscriptMap[ch] ?: ch)
                        }
                        i = closeParen + 1
                    } else {
                        sb.append('_')
                    }
                } else if (i < text.length) {
                    val ch = text[i]
                    sb.append(subscriptMap[ch] ?: ch)
                    i++
                }
            } else {
                sb.append(text[i])
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Converts a LaTeX mathematical string into formatted math Unicode typography.
     * Handles \frac, \sqrt, \mathbb, \setminus, greek symbols, operators, superscripts, subscripts, etc.
     */
    fun latexToUnicode(rawLatex: String): String {
        var s = rawLatex.trim()
            .removePrefix("$$").removeSuffix("$$")
            .removePrefix("$").removeSuffix("$")
            .trim()

        // Replace spacing commands
        s = s.replace("\\qquad", "    ")
            .replace("\\quad", "  ")
            .replace("\\,", " ")
            .replace("\\;", " ")
            .replace("\\ ", " ")

        // Replace left/right bracket delimiters
        s = s.replace("\\left(", "(")
            .replace("\\right)", ")")
            .replace("\\left[", "[")
            .replace("\\right]", "]")
            .replace("\\left\\{", "{")
            .replace("\\right\\}", "}")
            .replace("\\left|", "|")
            .replace("\\right|", "|")
            .replace("\\left.", "")
            .replace("\\right.", "")
            .replace("\\{", "{")
            .replace("\\}", "}")

        // Replace blackboard bold sets
        s = s.replace("\\mathbb{R}", "ℝ")
            .replace("\\mathbb{N}", "ℕ")
            .replace("\\mathbb{Z}", "ℤ")
            .replace("\\mathbb{Q}", "ℚ")
            .replace("\\mathbb{C}", "ℂ")
            .replace("\\mathbb{P}", "ℙ")

        // Replace \text{...}, \mathrm{...}, \mathbf{...}, \operatorname{...}
        s = replaceBracedMacro(s, "\\text")
        s = replaceBracedMacro(s, "\\mathrm")
        s = replaceBracedMacro(s, "\\mathbf")
        s = replaceBracedMacro(s, "\\mathit")
        s = replaceBracedMacro(s, "\\operatorname")

        // Replace set and logic symbols
        s = s.replace("\\setminus", " ∖ ")
            .replace("\\in", " ∈ ")
            .replace("\\notin", " ∉ ")
            .replace("\\subset", " ⊂ ")
            .replace("\\subseteq", " ⊆ ")
            .replace("\\cup", " ∪ ")
            .replace("\\cap", " ∩ ")
            .replace("\\emptyset", " ∅ ")
            .replace("\\forall", " ∀ ")
            .replace("\\exists", " ∃ ")
            .replace("\\implies", " ⟹ ")
            .replace("\\iff", " ⟺ ")
            .replace("\\to", " → ")
            .replace("\\rightarrow", " → ")
            .replace("\\leftarrow", " ← ")

        // Replace math operators
        s = s.replace("\\infty", "∞")
            .replace("\\pm", "±")
            .replace("\\mp", "∓")
            .replace("\\cdot", " · ")
            .replace("\\times", " × ")
            .replace("\\div", " ÷ ")
            .replace("\\circ", " ∘ ")
            .replace("\\approx", " ≈ ")
            .replace("\\neq", " ≠ ")
            .replace("\\ne", " ≠ ")
            .replace("\\leq", " ≤ ")
            .replace("\\le", " ≤ ")
            .replace("\\geq", " ≥ ")
            .replace("\\ge", " ≥ ")
            .replace("\\equiv", " ≡ ")

        // Replace math environments and alignments
        s = s.replace("\\begin{aligned}", "")
            .replace("\\end{aligned}", "")
            .replace("\\begin{array}", "")
            .replace("\\end{array}", "")
            .replace("\\begin{matrix}", "")
            .replace("\\end{matrix}", "")
            .replace("&=", " = ")
            .replace("&", " ")
            .replace("\\\\", "\n")

        // Replace common statistical accents: \bar{x}, \bar{X}, \overline{x}, \hat{y}
        s = s.replace("\\bar{x}", "x̄")
            .replace("\\bar{X}", "X̄")
            .replace("\\bar{y}", "ȳ")
            .replace("\\bar{Y}", "Ȳ")
            .replace("\\overline{x}", "x̄")
            .replace("\\overline{X}", "X̄")
            .replace("\\overline{y}", "ȳ")
            .replace("\\hat{y}", "ŷ")
            .replace("\\hat{x}", "x̂")
            .replace("\\hat{p}", "p̂")
            .replace("\\bar", "")
            .replace("\\overline", "")
            .replace("\\hat", "")

        // Replace Greek symbols
        s = s.replace("\\alpha", "α")
            .replace("\\beta", "β")
            .replace("\\gamma", "γ")
            .replace("\\delta", "δ")
            .replace("\\epsilon", "ε")
            .replace("\\varepsilon", "ε")
            .replace("\\theta", "θ")
            .replace("\\lambda", "λ")
            .replace("\\mu", "μ")
            .replace("\\pi", "π")
            .replace("\\sigma", "σ")
            .replace("\\phi", "φ")
            .replace("\\varphi", "φ")
            .replace("\\omega", "ω")
            .replace("\\Delta", "Δ")
            .replace("\\Sigma", "Σ")
            .replace("\\Omega", "Ω")

        // Replace standard functions
        s = s.replace("\\sin", "sin")
            .replace("\\cos", "cos")
            .replace("\\tan", "tan")
            .replace("\\sec", "sec")
            .replace("\\csc", "csc")
            .replace("\\cot", "cot")
            .replace("\\arcsin", "arcsin")
            .replace("\\arccos", "arccos")
            .replace("\\arctan", "arctan")
            .replace("\\ln", "ln")
            .replace("\\log", "log")
            .replace("\\exp", "exp")
            .replace("\\lim", "lim")
            .replace("\\sum", "∑")
            .replace("\\prod", "∏")
            .replace("\\int", "∫")

        // Replace \frac{num}{den} and \dfrac{num}{den} and \tfrac{num}{den}
        s = replaceFractions(s)

        // Replace \sqrt[n]{arg} and \sqrt{arg}
        s = replaceSquareRoots(s)

        // Format LaTeX exponents: ^{...} and ^x
        s = formatBracedExponents(s)

        // Format LaTeX subscripts: _{...} and _x
        s = formatBracedSubscripts(s)

        // Finally apply standard unicode math rules
        return formatToUnicode(s)
    }

    private fun replaceBracedMacro(text: String, macro: String): String {
        var result = text
        while (true) {
            val idx = result.indexOf("$macro{")
            if (idx == -1) break
            val openBrace = idx + macro.length
            val closeBrace = findMatchingBrace(result, openBrace)
            if (closeBrace == -1) break
            val inner = result.substring(openBrace + 1, closeBrace)
            result = result.substring(0, idx) + inner + result.substring(closeBrace + 1)
        }
        return result
    }

    private fun replaceFractions(text: String): String {
        var result = text
        val fracPatterns = listOf("\\frac", "\\dfrac", "\\tfrac")
        for (pat in fracPatterns) {
            while (true) {
                val idx = result.indexOf(pat)
                if (idx == -1) break

                var cursor = idx + pat.length
                while (cursor < result.length && result[cursor].isWhitespace()) cursor++
                if (cursor >= result.length || result[cursor] != '{') break

                val numOpen = cursor
                val numClose = findMatchingBrace(result, numOpen)
                if (numClose == -1) break

                cursor = numClose + 1
                while (cursor < result.length && result[cursor].isWhitespace()) cursor++
                if (cursor >= result.length || result[cursor] != '{') break

                val denOpen = cursor
                val denClose = findMatchingBrace(result, denOpen)
                if (denClose == -1) break

                val numerator = result.substring(numOpen + 1, numClose).trim()
                val denominator = result.substring(denOpen + 1, denClose).trim()

                val formattedNum = latexToUnicode(numerator)
                val formattedDen = latexToUnicode(denominator)

                val numNeedsParen = formattedNum.contains("+") || formattedNum.contains("-") || formattedNum.contains("·")
                val denNeedsParen = formattedDen.contains("+") || formattedDen.contains("-") || formattedDen.contains("·")

                val numStr = if (numNeedsParen) "($formattedNum)" else formattedNum
                val denStr = if (denNeedsParen) "($formattedDen)" else formattedDen

                val replacement = "$numStr / $denStr"
                result = result.substring(0, idx) + replacement + result.substring(denClose + 1)
            }
        }
        return result
    }

    private fun replaceSquareRoots(text: String): String {
        var result = text
        while (true) {
            val idx = result.indexOf("\\sqrt")
            if (idx == -1) break

            var cursor = idx + 5
            while (cursor < result.length && result[cursor].isWhitespace()) cursor++
            if (cursor >= result.length) break

            var rootDegree = ""
            if (result[cursor] == '[') {
                val closeBracket = result.indexOf(']', cursor)
                if (closeBracket != -1) {
                    val deg = result.substring(cursor + 1, closeBracket).trim()
                    rootDegree = deg.map { superscriptMap[it] ?: it }.joinToString("")
                    cursor = closeBracket + 1
                    while (cursor < result.length && result[cursor].isWhitespace()) cursor++
                }
            }

            if (cursor < result.length && result[cursor] == '{') {
                val openBrace = cursor
                val closeBrace = findMatchingBrace(result, openBrace)
                if (closeBrace == -1) break

                val inner = result.substring(openBrace + 1, closeBrace).trim()
                val formattedInner = latexToUnicode(inner)
                val prefix = if (rootDegree.isNotEmpty()) "$rootDegree√" else "√"
                val replacement = "$prefix($formattedInner)"
                result = result.substring(0, idx) + replacement + result.substring(closeBrace + 1)
            } else {
                break
            }
        }
        return result
    }

    private fun formatBracedExponents(text: String): String {
        var result = text
        while (true) {
            val idx = result.indexOf("^{")
            if (idx == -1) break
            val openBrace = idx + 1
            val closeBrace = findMatchingBrace(result, openBrace)
            if (closeBrace == -1) break

            val exp = result.substring(openBrace + 1, closeBrace)
            val formattedExp = exp.map { superscriptMap[it] ?: it }.joinToString("")
            result = result.substring(0, idx) + formattedExp + result.substring(closeBrace + 1)
        }
        return result
    }

    private fun formatBracedSubscripts(text: String): String {
        var result = text
        while (true) {
            val idx = result.indexOf("_{")
            if (idx == -1) break
            val openBrace = idx + 1
            val closeBrace = findMatchingBrace(result, openBrace)
            if (closeBrace == -1) break

            val sub = result.substring(openBrace + 1, closeBrace)
            val formattedSub = sub.map { subscriptMap[it] ?: it }.joinToString("")
            result = result.substring(0, idx) + formattedSub + result.substring(closeBrace + 1)
        }
        return result
    }

    private fun findMatchingBrace(text: String, openIdx: Int): Int {
        var depth = 0
        for (i in openIdx until text.length) {
            if (text[i] == '{') depth++
            else if (text[i] == '}') {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }

    /**
     * Splits an equation or expression into numerator/denominator if it's a top-level fraction
     */
    fun splitTopLevelFraction(expr: String): Pair<String, String>? {
        val clean = expr.trim()

        // Check LaTeX fraction \frac{A}{B}, \dfrac{A}{B}, \tfrac{A}{B}
        val fracPatterns = listOf("\\frac", "\\dfrac", "\\tfrac")
        for (pat in fracPatterns) {
            if (clean.startsWith(pat)) {
                var cursor = pat.length
                while (cursor < clean.length && clean[cursor].isWhitespace()) cursor++
                if (cursor < clean.length && clean[cursor] == '{') {
                    val numOpen = cursor
                    val numClose = findMatchingBrace(clean, numOpen)
                    if (numClose != -1) {
                        cursor = numClose + 1
                        while (cursor < clean.length && clean[cursor].isWhitespace()) cursor++
                        if (cursor < clean.length && clean[cursor] == '{') {
                            val denOpen = cursor
                            val denClose = findMatchingBrace(clean, denOpen)
                            if (denClose == clean.length - 1) {
                                val num = clean.substring(numOpen + 1, numClose).trim()
                                val den = clean.substring(denOpen + 1, denClose).trim()
                                if (num.isNotEmpty() && den.isNotEmpty()) {
                                    return Pair(num, den)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check if expression is of form (A)/(B) or A/B
        var depth = 0
        var divIdx = -1
        for (i in clean.indices) {
            when (clean[i]) {
                '(' -> depth++
                ')' -> depth--
                '/' -> if (depth == 0) {
                    divIdx = i
                    break
                }
            }
        }
        if (divIdx != -1) {
            val num = clean.substring(0, divIdx).trim().removeSurrounding("(", ")")
            val den = clean.substring(divIdx + 1).trim().removeSurrounding("(", ")")
            if (num.isNotEmpty() && den.isNotEmpty()) {
                return Pair(num, den)
            }
        }
        return null
    }
}
