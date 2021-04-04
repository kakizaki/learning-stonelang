package ast

class BinaryExpr : ASTList {
    constructor(
        left: ASTree,
        operator: ASTree,
        right: ASTree
    ): super(listOf(left, operator, right)) {
    }

    fun left(): ASTree {
        return child(0)
    }

    fun operator(): String {
        return (child(1) as ASTLeaf).token.getText()
    }

    fun right(): ASTree {
        return child(2)
    }
}