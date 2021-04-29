import eval.Evaluator
import parser.parseProgram
import java.io.StringReader

object Util {

    fun Evaluator.evalCode(code: String): Any? {
        val reader = StringReader(code)
        val lexer = Lexer(reader)

        var last: Any? = null
        while (lexer.peek(0) != Token.EOF) {
            val tree = parseProgram(lexer)
            last = this.eval(tree)
        }
        return last
    }

    fun Evaluator.evalCodeError(code: String): Exception? {
        try {
            this.evalCode(code)
            return null
        }
        catch (e: Exception) {
            return e
        }
    }
}