import Util.evalCode
import Util.evalCodeError
import eval.Evaluator
import junit.framework.Assert.assertNotNull
import kotlin.test.Test
import kotlin.test.assertSame

class TestFunc {
    @Test
    fun callFun() {
        val s = """
a = 13
b = 24
def f() { 1 }
def f2(a) { a + 1 }
def f3(a, b) { a + b }
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)

        assertSame(1, evaluator.evalCode("f()"))
        assertSame(3, evaluator.evalCode("f2(2)"))
        assertSame(5, evaluator.evalCode("f3(1, 4)"))
        assertSame(13, evaluator.evalCode("a"))
        assertSame(24, evaluator.evalCode("b"))

        assertNotNull(evaluator.evalCodeError("f3(1,)"))
    }
}