package ast

class FuncCall: ASTList {
    companion object {
        fun toList(a: ASTree, b: List<ASTree>): List<ASTree> {
            val l = mutableListOf<ASTree>()
            l.add(a)
            l.addAll(b)
            return l
        }
    }

    constructor(
        funcName: Name,
        args: List<ASTree>
    ) : super(toList(funcName, args)) {
        this.funcName = funcName
        this.args = args
    }

    val funcName: Name
    val args: List<ASTree>

    override fun toString(): String {
        val sb = StringBuilder()
        for (n in args) {
            if (sb.isEmpty() == false) {
                sb.append(", ")
            }
            sb.append("$n")
        }
        return "($funcName ($sb))"
    }
}