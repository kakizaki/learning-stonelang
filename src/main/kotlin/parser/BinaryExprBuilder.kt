package parser

import Token
import ast.ASTLeaf
import ast.ASTree
import ast.BinaryExpr


class BinaryExprBuilder(
    private val operatorSet: BinaryOperatorSet
) {
    data class LeftAndOperator(
        val left: ASTree,
        val opToken: Token,
        val op: BinaryOperator
    ) { }

    private val shiftElement = mutableListOf<LeftAndOperator>()

    fun clear() {
        shiftElement.clear()
    }

    fun last(f: ASTree): ASTree {
        var right = f
        for (n in shiftElement.asReversed()) {
            right = BinaryExpr(n.left, ASTLeaf(n.opToken), right)
        }
        return right
    }

    fun next(f: ASTree, opToken: Token) {
        var right = f

        val op = operatorSet.getOrNull(opToken.getText())
        if (op == null) {
            throw Exception("unsupported operator")
        }

        // right より積まれた要素の演算子が優先の場合は、right と積まれた要素と繋げて right にする
        while (shiftElement.isEmpty() == false) {
            val last = shiftElement.last()
            val bindLastOp = when {
                op.precedence < last.op.precedence -> true
                op.precedence > last.op.precedence -> false
                else -> last.op.leftJoin
            }
            if (bindLastOp) {
                right = BinaryExpr(last.left, ASTLeaf(last.opToken), right)
                shiftElement.removeLast()
            } else {
                break
            }
        }

        // right を積んで次の element を待つ
        shiftElement.add(LeftAndOperator(right, opToken, op))
    }
}




