package ast

class ArrayDef: ASTList {
    constructor(
        elements: List<ASTree>
    ) : super(listOf(*elements.toTypedArray())) {
        this.elements = elements
    }

    val elements: List<ASTree>

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[")
        for (n in elements) {
            if (sb.isNotEmpty()) {
                sb.append(", ")
            }
            sb.append("$n")
        }
        sb.append("]")
        return "($sb)"
    }
}