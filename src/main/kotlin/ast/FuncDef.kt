package ast

class FuncDef: ASTList {
    companion object {
        fun toList(a: ASTree, b: List<ASTree>, c: ASTree): List<ASTree> {
            val l = mutableListOf<ASTree>()
            l.add(a)
            l.addAll(b)
            l.add(c)
            return l
        }
    }

    constructor(
        funcName: Name,
        params: List<Name>,
        body: BlockStatement
    ) : super(toList(funcName, params, body)) {
        this.funcName = funcName
        this.params = params
        this.body = body
    }

    val funcName: Name
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
        return "(def $funcName ($sb) $body)"
    }
}