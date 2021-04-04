import ast.*
import parser.parseBlock
import parser.parseExpr
import parser.parseProgram
import parser.parseStatement
import java.io.StringReader

fun main(args: Array<String>) {
//    val lexer = Lexer(CodeDialog())
//    while (true) {
//        val t = lexer.read()
//        if (t == Token.EOF) {
//            break
//        }
//        println("=> ${t.getText()} : ${t::class.simpleName}")
//    }

    val test = listOf(
        "1 + 2 + 3",
        "1 + 2 * 3",
        "1 + 2 + 3 * 4",
        "1 + 2 * 3 / 4",
        "1 + 2 * 3 / 4 + 5",
        "1 + 2 * 3 / (4 + 5)",
        "x = y = 1",
        "1 + -2",
    )
    for (s in test) {
        val l = Lexer(StringReader(s))
        val tree = parseExpr(l)
        println(" input: $s")
        println("output: ${tree.toString()}")
    }

    val test2 = listOf(
        "{ i = 2; i = 3; }"
    )
    for (s in test2) {
        val l = Lexer(StringReader(s))
        val tree = parseBlock(l)
        println(" input: $s")
        println("output: ${tree.toString()}")
    }

    val test3 = listOf(
        """
even = 0
odd = 0
i = 1
while i < 10 {
  if i % 2 == 0 {
    even = even + i
  } else {
    odd = odd + i
  }
  i = i + 1
}
even + odd
        """.trimIndent()
    )
    for (s in test3) {
        println(" input: $s")
        val l = Lexer(StringReader(s))
        while (l.peek(0) != Token.EOF) {
            val tree = parseProgram(l)
            println("output: ${tree.toString()}")
        }
    }
}









