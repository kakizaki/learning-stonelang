package ast

class IfStatement: ASTList {

    constructor(
        condition:ASTree,
        thenBlock:ASTree
    ) : super(listOf(condition, thenBlock)) {
        hasElseBlock = false
    }

    constructor(
        condition:ASTree,
        thenBlock:ASTree,
        elseBlock:ASTree
    ) : super(listOf(condition, thenBlock, elseBlock)) {
        hasElseBlock = true
    }

    val hasElseBlock: Boolean

    fun condition(): ASTree = child(0)
    fun thenBlock(): ASTree = child(1)
    fun elseBlock(): ASTree? {
        if (hasElseBlock) return child(2)
        return null
    }

    override fun toString(): String {
        return "(if ${condition()} ${thenBlock()} else ${elseBlock()})"
    }


}