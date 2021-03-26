

abstract class Token(val lineNumber: Int) {
    companion object {
        val EOF = object: Token(-1) {}
        val EOL = "\\n"
    }

    open fun isIdentifier(): Boolean {
        return false
    }

    open fun isNumber(): Boolean {
        return false
    }

    open fun isString(): Boolean {
        return false
    }

    open fun getNumber(): Int {
        throw StoneException("not number token")
    }

    open fun getText(): String {
        return ""
    }
}


class NumToken(line: Int, private val v: Int): Token(line) {
    override fun isNumber(): Boolean {
        return true
    }

    override fun getText(): String {
        return v.toString()
    }

    override fun getNumber(): Int {
        return v
    }
}


class IdToken(line: Int, private val id: String): Token(line) {
    override fun isIdentifier(): Boolean {
        return true
    }

    override fun getText(): String {
        return id
    }
}


class StringToken(line: Int, private val s: String): Token(line) {
    override fun isString(): Boolean {
        return true
    }

    override fun getText(): String {
        return s
    }
}












