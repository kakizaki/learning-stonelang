import java.io.IOException
import kotlin.contracts.contract

class ParseException : Exception {
    companion object {
        fun location(t: Token): String {
            if (t == Token.EOF) {
                return "the last line"
            }
            return "\"${t.getText()}\" at line ${t.lineNumber}"
        }
    }

    constructor(msg: String, t: Token)
            : super("syntax error around ${location(t)}. $msg") {
    }

    constructor(t: Token) : this("", t) {
    }

    constructor(e: IOException) : super(e) {
    }

    constructor(msg: String): super(msg) {
    }
}