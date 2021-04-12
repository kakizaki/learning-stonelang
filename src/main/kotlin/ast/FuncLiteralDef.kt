package ast

class FuncLiteralDef: ASTList {
    companion object {
        fun toList(a: List<ASTree>, b: ASTree): List<ASTree> {
            val l = mutableListOf<ASTree>()
            l.addAll(a)
            l.add(b)
            return l
        }
    }

    constructor(
        params: List<Name>,
        body: BlockStatement
    ) : super(toList(params, body)) {
        this.params = params
        this.body = body
    }

    val params: List<Name>
    val body: BlockStatement

    override fun toString(): String {
        val sb = StringBuilder()
        for (n in params) {
            if (sb.isEmpty() == false) {
                sb.append(", ")
            }
            sb.append("$n")
        }
        return "(def ($sb) $body)"
    }
}