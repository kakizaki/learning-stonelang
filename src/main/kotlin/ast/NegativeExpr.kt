package ast

class NegativeExpr(list: List<ASTree>): ASTList(list) {
    fun operand(): ASTree {
        return child(0)
    }

    override fun toString(): String {
        return "-" + operand()
    }
}