package parser

interface BinaryOperatorSet {
    fun getOrNull(op: String): BinaryOperator?
    fun isOperator(op: String): Boolean
}


class BinaryOperatorContainer: BinaryOperatorSet {
    private val ops = mutableMapOf<String, BinaryOperator>()

    fun add(op: String, precedence: Int, leftJoin: Boolean) {
        ops[op] = BinaryOperator(op, precedence, leftJoin)
    }

    override fun getOrNull(op: String): BinaryOperator? {
        return ops[op]
    }

    override fun isOperator(op: String): Boolean {
        return ops.containsKey(op)
    }

}