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

    /**
     * 最後の単項
     * 式の全体が 1+2*3 であったとき、(f:3)
     */
    fun last(f: ASTree): ASTree {
        var right = f
        for (n in shiftElement.asReversed()) {
            right = BinaryExpr(n.left, ASTLeaf(n.opToken), right)
        }
        return right
    }

    /**
     * 単項と演算子
     * 式の全体が 1+2*3 であったとき、(f:1, opToken:+) , (f:2, opToken:*)
     */
    fun next(f: ASTree, opToken: Token) {
        var right = f

        val op = operatorSet.getOrNull(opToken.getText())
        if (op == null) {
            throw Exception("unsupported operator")
        }

        // 引数の演算子より積まれた要素の演算子を優先する場合は、積まれた要素と right で式を作り right にセットする
        // 上記が可能な間は繰り返し
        while (shiftElement.isEmpty() == false) {
            val last = shiftElement.last()
            val combineLastOp = when {
                op.precedence < last.op.precedence -> true
                op.precedence > last.op.precedence -> false
                else -> last.op.leftJoin
            }
            if (combineLastOp) {
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




