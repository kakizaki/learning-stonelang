package ast

import Token

class Name(token: Token) : ASTLeaf(token) {
    fun name(): String {
        return token.getText()
    }
}
