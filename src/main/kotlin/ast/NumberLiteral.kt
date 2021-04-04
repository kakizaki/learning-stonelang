package ast

import Token

class NumberLiteral(token: Token) : ASTLeaf(token) {
    fun value(): Int {
        return token.getNumber()
    }
}
