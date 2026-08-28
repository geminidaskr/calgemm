package com.example.math

import kotlin.math.*

/**
 * Fast recursive-descent math parser and numerical evaluator designed for high-FPS graphing.
 */
class MathParser(private val rawExpression: String) {

    private val tokenized: List<Token>

    init {
        tokenized = tokenize(preprocess(rawExpression))
    }

    /**
     * Evaluates f(x) safely. Returns Double.NaN if undefined.
     */
    fun evaluate(x: Double): Double {
        return try {
            val parser = Evaluator(tokenized, x)
            val result = parser.parseExpression()
            if (result.isInfinite() || result.isNaN()) Double.NaN else result
        } catch (_: Exception) {
            Double.NaN
        }
    }

    /**
     * Evaluates f(x) for an array of x points quickly.
     */
    fun evaluateRange(xValues: DoubleArray): DoubleArray {
        val yValues = DoubleArray(xValues.size)
        for (i in xValues.indices) {
            yValues[i] = evaluate(xValues[i])
        }
        return yValues
    }

    companion object {
        fun sanitize(expr: String): String {
            return expr.trim()
                .replace(" ", "")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("–", "-")
                .replace("—", "-")
                .replace("√", "sqrt")
                .replace("sen", "sin")
                .replace("tg", "tan")
                .replace("arctg", "atan")
                .replace("arcsen", "asin")
        }

        private fun preprocess(expr: String): String {
            var s = sanitize(expr)
            if (s.isEmpty()) return "0"

            // Insert implicit multiplication: e.g. 2x -> 2*x, x(x+1) -> x*(x+1), (x+1)(x-1) -> (x+1)*(x-1)
            // 3sin(x) -> 3*sin(x), x e^x -> x*e^x
            val sb = StringBuilder()
            var i = 0
            while (i < s.length) {
                val c = s[i]
                sb.append(c)
                if (i < s.length - 1) {
                    val next = s[i + 1]
                    val isCurrentDigit = c.isDigit() || c == '.'
                    val isNextDigit = next.isDigit() || next == '.'
                    val isCurrentLetterOrClosing = c.isLetter() || c == ')' || isCurrentDigit
                    val isNextLetterOrOpening = next.isLetter() || next == '('

                    if ((isCurrentDigit && isNextLetterOrOpening && next != '.') ||
                        (c == ')' && (isNextDigit || isNextLetterOrOpening)) ||
                        (c == 'x' && (isNextDigit || isNextLetterOrOpening && next != '^')) ||
                        (c == 'e' && (isNextDigit || (isNextLetterOrOpening && next != '^' && next != 'x')))
                    ) {
                        // Check if current is part of a function name like 'sin', 'cos' etc.
                        val prevWord = getWordEndingAt(s, i)
                        if (!isKnownFunction(prevWord)) {
                            sb.append('*')
                        }
                    }
                }
                i++
            }
            return sb.toString()
        }

        private fun getWordEndingAt(s: String, endIdx: Int): String {
            var start = endIdx
            while (start >= 0 && s[start].isLetter()) {
                start--
            }
            return s.substring(start + 1, endIdx + 1)
        }

        private fun isKnownFunction(name: String): Boolean {
            return name in listOf("sin", "cos", "tan", "cot", "sec", "csc", "asin", "acos", "atan", "sinh", "cosh", "tanh", "sqrt", "cbrt", "abs", "ln", "log", "exp", "floor", "ceil", "sgn")
        }

        private fun tokenize(expr: String): List<Token> {
            val tokens = mutableListOf<Token>()
            var i = 0
            while (i < expr.length) {
                val c = expr[i]
                when {
                    c.isWhitespace() -> i++
                    c.isDigit() || c == '.' -> {
                        val start = i
                        while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                            i++
                        }
                        val numStr = expr.substring(start, i)
                        tokens.add(Token.Number(numStr.toDoubleOrNull() ?: 0.0))
                    }
                    c.isLetter() || c == 'π' -> {
                        val start = i
                        while (i < expr.length && (expr[i].isLetter() || expr[i] == 'π')) {
                            i++
                        }
                        val ident = expr.substring(start, i)
                        when (ident.lowercase()) {
                            "x" -> tokens.add(Token.Variable("x"))
                            "pi", "π" -> tokens.add(Token.Number(Math.PI))
                            "e" -> tokens.add(Token.Number(Math.E))
                            else -> tokens.add(Token.Function(ident.lowercase()))
                        }
                    }
                    c == '+' -> { tokens.add(Token.Op('+')); i++ }
                    c == '-' -> { tokens.add(Token.Op('-')); i++ }
                    c == '*' -> { tokens.add(Token.Op('*')); i++ }
                    c == '/' -> { tokens.add(Token.Op('/')); i++ }
                    c == '^' -> { tokens.add(Token.Op('^')); i++ }
                    c == '(' -> { tokens.add(Token.OpenParen); i++ }
                    c == ')' -> { tokens.add(Token.CloseParen); i++ }
                    c == '|' -> { tokens.add(Token.Pipe); i++ }
                    c == ',' -> { tokens.add(Token.Comma); i++ }
                    else -> i++
                }
            }
            return tokens
        }
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Variable(val name: String) : Token()
        data class Function(val name: String) : Token()
        data class Op(val char: Char) : Token()
        object OpenParen : Token()
        object CloseParen : Token()
        object Pipe : Token()
        object Comma : Token()
    }

    private class Evaluator(private val tokens: List<Token>, private val xVal: Double) {
        private var pos = 0

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): Double {
            var result = parseTerm()
            while (pos < tokens.size) {
                when (val tok = peek()) {
                    is Token.Op -> {
                        if (tok.char == '+' || tok.char == '-') {
                            consume()
                            val nextTerm = parseTerm()
                            result = if (tok.char == '+') result + nextTerm else result - nextTerm
                        } else {
                            break
                        }
                    }
                    else -> break
                }
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (pos < tokens.size) {
                when (val tok = peek()) {
                    is Token.Op -> {
                        if (tok.char == '*' || tok.char == '/') {
                            consume()
                            val nextFactor = parseFactor()
                            result = if (tok.char == '*') {
                                result * nextFactor
                            } else {
                                if (abs(nextFactor) < 1e-15) Double.NaN else result / nextFactor
                            }
                        } else {
                            break
                        }
                    }
                    else -> break
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            var result = parseUnary()
            if (pos < tokens.size) {
                val tok = peek()
                if (tok is Token.Op && tok.char == '^') {
                    consume()
                    val exponent = parseFactor() // Right associative
                    result = if (result < 0 && (exponent % 1.0 != 0.0)) {
                        // Check if exponent is rational e.g. 1/3 (cube root of negative is valid real number)
                        val denom = round(1.0 / exponent)
                        if (abs(denom * exponent - 1.0) < 1e-6 && denom.toInt() % 2 != 0) {
                            -((-result).pow(exponent))
                        } else {
                            Double.NaN
                        }
                    } else {
                        result.pow(exponent)
                    }
                }
            }
            return result
        }

        private fun parseUnary(): Double {
            val tok = peek() ?: return 0.0
            if (tok is Token.Op && (tok.char == '+' || tok.char == '-')) {
                consume()
                val factor = parseUnary()
                return if (tok.char == '-') -factor else factor
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Double {
            val tok = peek() ?: return Double.NaN
            when (tok) {
                is Token.Number -> {
                    consume()
                    return tok.value
                }
                is Token.Variable -> {
                    consume()
                    return xVal
                }
                is Token.Function -> {
                    consume()
                    val fnName = tok.name
                    // Expect (arg)
                    var hasParen = false
                    if (peek() is Token.OpenParen) {
                        consume()
                        hasParen = true
                    }
                    val arg = parseExpression()
                    if (hasParen && peek() is Token.CloseParen) {
                        consume()
                    }
                    return evalFunction(fnName, arg)
                }
                is Token.OpenParen -> {
                    consume()
                    val expr = parseExpression()
                    if (peek() is Token.CloseParen) {
                        consume()
                    }
                    return expr
                }
                is Token.Pipe -> { // Absolute value |x|
                    consume()
                    val expr = parseExpression()
                    if (peek() is Token.Pipe) {
                        consume()
                    }
                    return abs(expr)
                }
                else -> {
                    consume()
                    return Double.NaN
                }
            }
        }

        private fun evalFunction(name: String, arg: Double): Double {
            if (arg.isNaN()) return Double.NaN
            return when (name) {
                "sin" -> sin(arg)
                "cos" -> cos(arg)
                "tan" -> {
                    val c = cos(arg)
                    if (abs(c) < 1e-12) Double.NaN else tan(arg)
                }
                "cot" -> {
                    val s = sin(arg)
                    if (abs(s) < 1e-12) Double.NaN else 1.0 / tan(arg)
                }
                "sec" -> {
                    val c = cos(arg)
                    if (abs(c) < 1e-12) Double.NaN else 1.0 / c
                }
                "csc" -> {
                    val s = sin(arg)
                    if (abs(s) < 1e-12) Double.NaN else 1.0 / s
                }
                "asin", "arcsin" -> if (arg in -1.0..1.0) asin(arg) else Double.NaN
                "acos", "arccos" -> if (arg in -1.0..1.0) acos(arg) else Double.NaN
                "atan", "arctan" -> atan(arg)
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "sqrt" -> if (arg >= 0) sqrt(arg) else Double.NaN
                "cbrt" -> if (arg >= 0) arg.pow(1.0 / 3.0) else -((-arg).pow(1.0 / 3.0))
                "abs" -> abs(arg)
                "ln" -> if (arg > 0) ln(arg) else Double.NaN
                "log", "log10" -> if (arg > 0) log10(arg) else Double.NaN
                "exp" -> exp(arg)
                "floor" -> floor(arg)
                "ceil" -> ceil(arg)
                "sgn", "sign" -> sign(arg)
                else -> Double.NaN
            }
        }
    }
}
