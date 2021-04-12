package parser

import Lexer
import ast.ASTLeaf
import ast.ASTList
import ast.ASTree



class Precedence(val value: Int, val leftAssoc: Boolean) {
}


class OperationParseTest {

    val ops  = mutableMapOf<String, Precedence>()

    constructor() {
        ops["="] = Precedence(1, false)
        ops["+"] = Precedence(3, true)
        ops["-"] = Precedence(3, true)
        ops["*"] = Precedence(4, true)
        ops["/"] = Precedence(4, true)
    }

    fun parse(l: Lexer, res: MutableList<ASTree>) {
        var right = parseFactor(l)

        var prec: Precedence? = null
        while (true) {
            prec = nextOperator(l)
            if (prec == null) {
                break
            }
            right = doShift(l, right, prec.value)
        }
        res.add(right)
    }

    fun nextOperator(l: Lexer): Precedence? {
        val t = l.peek(0)
        if (t.isIdentifier()) {
            return ops.get(t.getText())
        } else {
            return null
        }
    }

    fun doShift(l: Lexer, left: ASTree, prec: Int): ASTree {
        val list = mutableListOf<ASTree>()
        list.add(left)
        list.add(ASTLeaf(l.read()))

        var right = parseFactor(l)
        var next: Precedence? = null
        while (true) {
            next = nextOperator(l)
            if (next != null && rightIsExpr(prec, next)) {
                right = doShift(l, right, next.value)
            } else {
                break
            }
        }
        list.add(right)
        return ASTList(list)
    }

    fun rightIsExpr(prec: Int, next: Precedence): Boolean {
        if (next.leftAssoc) {
            return prec < next.value
        } else {
            return prec <= next.value
        }
    }
}