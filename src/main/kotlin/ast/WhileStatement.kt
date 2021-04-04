package ast

class WhileStatement: ASTList {

    constructor(
        condition: ASTree,
        body: ASTree
    ) : super(listOf(condition, body)) {
    }

    fun condition(): ASTree = child(0)
    fun body(): ASTree = child(1)

    override fun toString(): String {
        return "(while ${condition()} ${body()})"
    }

}