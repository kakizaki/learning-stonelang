package ast

import Token
import java.lang.IndexOutOfBoundsException

open class ASTLeaf(val token: Token) : ASTree() {
    companion object {
        val empty = emptyList<ASTree>()
    }

    override fun child(i: Int): ASTree {
        throw IndexOutOfBoundsException()
    }

    override fun numChildren(): Int {
        return 0
    }

    override fun children(): Iterator<ASTree> {
        return empty.iterator()
    }

    override fun location(): String? {
        return "at line ${token.lineNumber}"
    }

    override fun toString(): String {
        return token.getText()
    }
}