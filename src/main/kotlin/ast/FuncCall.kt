package ast

class FuncCall: ASTList {
    constructor(
        callee: ASTree,
        args: List<ASTree>
    ) : super(listOf(callee, *args.toTypedArray())) {
        this.callee = callee
        this.args = args
    }

    val callee: ASTree
    val args: List<ASTree>

    override fun toString(): String {
        val sb = StringBuilder()
        for (n in args) {
            if (sb.isNotEmpty()) {
                sb.append(", ")
            }
            sb.append("$n")
        }
        return "($callee ($sb))"
    }
}