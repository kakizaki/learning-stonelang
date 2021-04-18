import ast.ASTree
import eval.Evaluator
import parser.OperationParseTest
import parser.parseBlock
import parser.parseExpr
import parser.parseProgram
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
        "x = x + y + 1",
        "x = x + y - 1",
        "x = x + y - 1",
        "x = y = x + y + 1 * 2 + 1",
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
def counter(c) { 
  def () { c = c + 1 }
}            
b = counter(0)
c = counter(1)
b()
c()
b()
c()
        """.trimIndent(),
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
def aaa(a, b, c) {
    d = a + b + c
}
def bbb() {
    d = 34
    aaa(d, 1, 2)
    d
}
aaa(1, 2, 3)
bbb()
even + odd
print(even)
s = "1234"
length(s)
i = toInt(s)
currentTime()
r = read()
        """.trimIndent()
    )

    val evaluator = Evaluator()
    for (s in test3) {
        println(" input: $s")
        val l = Lexer(StringReader(s))
        while (l.peek(0) != Token.EOF) {
            val tree = parseProgram(l)
            println("tree: ${tree.toString()}")
            val r = evaluator.eval(tree)
            println("=> $r")
        }
    }


}









