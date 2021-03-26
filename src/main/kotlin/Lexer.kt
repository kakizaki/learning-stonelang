import java.io.IOException
import java.io.LineNumberReader
import java.io.Reader
import java.lang.reflect.InvocationTargetException
import java.util.regex.Matcher
import java.util.regex.Pattern

class Lexer(r: Reader) {
    companion object {
        val regexPat = "\\s*(" +
                "(//.*)" + // comment
                "|([0-9]+)" + // number literal
                "|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")" + // string literal
                "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct}" + // identifier
                ")?"
    }

    private val pattern = Pattern.compile(regexPat)

    private val queue = mutableListOf<Token>()

    private var hasMore: Boolean

    private val reader: LineNumberReader

    init {
        reader = LineNumberReader(r)
        hasMore = true
    }

    fun read(): Token {
        return if (fillQueue(0))  {
            queue.removeAt(0)
        } else {
            Token.EOF
        }
    }

    fun peek(i: Int): Token {
        return if (fillQueue(i)) {
            queue[i]
        } else {
            Token.EOF
        }
    }

    private fun fillQueue(i: Int): Boolean {
        if (i < queue.size) {
            return true
        }
        while (hasMore) {
            readLine()
            if (i < queue.size) {
                return true
            }
        }
        return false
    }

    private fun readLine() {
        var line: String? = null
        try {
            line = reader.readLine()
        } catch (e: IOException) {
            throw ParseException(e)
        }

        if (line == null) {
            hasMore = false
            return
        }

        val lineNumber = reader.lineNumber
        val matcher = pattern.matcher(line)
        matcher.useTransparentBounds(true).useAnchoringBounds(false)

        var pos = 0
        val endPos = line.length
        while (pos < endPos) {
            matcher.region(pos, endPos)
            if (matcher.lookingAt()) {
                addToken(lineNumber, matcher)
                pos = matcher.end()
            }
            else {
                throw ParseException("bad token at line $lineNumber")
            }
        }
        queue.add(IdToken(lineNumber, Token.EOL))
    }

    private fun addToken(lineNumber: Int, matcher: Matcher) {
        val m = matcher.group(1)
        if (m == null) {
            return
        }

        //  コメントにマッチした
        if (matcher.group(2) != null) {
            return
        }

        val token = when {
            matcher.group(3) != null -> {
                NumToken(lineNumber, Integer.parseInt(m))
            }
            matcher.group(4) != null -> {
                StringToken(lineNumber, toStringLiteral(m))
            }
            else -> {
                IdToken(lineNumber, m)
            }
        }
        queue.add(token)
    }

    private fun toStringLiteral(s: String): String {
        val b = StringBuilder()
        val len = s.length - 1

        var i = 1
        while (i < len) {
            var c = s[i]
            if (c == '\\' && i + 1 < len) {
                tryUnescape(s[i + 1])?.let {
                    c = it
                    ++i
                }
            }
            b.append(c)
            ++i
        }
        return b.toString()
    }

    private fun tryUnescape(c: Char): Char? {
        when (c) {
            '"', '\\' -> {
                return c
            }
            'n' -> {
                return '\n'
            }
        }
        return null
    }

}

