import java.io.IOException
import java.io.Reader
import java.io.StringReader
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea

fun main(args: Array<String>) {
    val lexer = Lexer(CodeDialog())
    while (true) {
        val t = lexer.read()
        if (t == Token.EOF) {
            break
        }
        println("=> ${t.getText()} : ${t::class.simpleName}")
    }
}


