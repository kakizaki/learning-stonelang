package ast

class VarDef(
    val name: Name,
    val value: ASTree
): ASTList(listOf(name, value)) {
    override fun toString(): String {
        return "($name = $value)"
    }
}