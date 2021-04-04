package ast

import Token

class StringLiteral(token: Token): ASTLeaf(token) {
    fun value(): String {
        return token.getText()
    }
}