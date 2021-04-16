package parser

data class BinaryOperator(
    val op: String,
    val precedence: Int,
    val leftJoin: Boolean
) {

}