import Util.evalCode
import Util.evalCodeError
import eval.Evaluator
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestArray {
    @Test
    fun arrayIndexer_Integer() {
        val s = """
a = [1, 2, 3, "123"] 
e = []
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        assertSame(1, evaluator.evalCode("a[0]"))
        assertSame(2, evaluator.evalCode("a[1]"))
        assertSame(3, evaluator.evalCode("a[2]"))
        assertTrue("123" == evaluator.evalCode("a[3]"))

        // index out of bounds
        assertNotNull(evaluator.evalCodeError("a[4]"))
        assertNotNull(evaluator.evalCodeError("e[0]"))
    }

    @Test
    fun arrayIndexer_Expr() {
        val s = """
a = [1, 2, 3, "123"] 
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        assertSame(1, evaluator.evalCode("a[0]"))
        assertSame(2, evaluator.evalCode("a[0+1]"))
        assertSame(3, evaluator.evalCode("a[1*2]"))
        assertTrue("123" == evaluator.evalCode("a[1+1*2]"))
    }

    @Test
    fun arrayIndexer_Variable() {
        val s = """
a = [1, 2, 3, "123"] 
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        evaluator.evalCode("i = 0")
        assertSame(1, evaluator.evalCode("a[i]"))
        evaluator.evalCode("i = 1")
        assertSame(2, evaluator.evalCode("a[i]"))
        evaluator.evalCode("i = 2")
        assertSame(3, evaluator.evalCode("a[i]"))
        evaluator.evalCode("i = 3")
        assertTrue("123" == evaluator.evalCode("a[i]"))
    }

    @Test
    fun arrayOfArray() {
        val s = """
a = [1, 2, [3, 4, 5], "123"] 
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        assertSame(1, evaluator.evalCode("a[0]"))
        assertSame(2, evaluator.evalCode("a[1]"))
        assertSame(3, evaluator.evalCode("a[2][0]"))
        assertSame(4, evaluator.evalCode("a[2][1]"))
        assertSame(5, evaluator.evalCode("a[2][2]"))
        assertTrue("123" == evaluator.evalCode("a[3]"))
    }

    @Test
    fun arrayOfArray_2() {
        val s = """
b = [3, 4, 5]
a = [1, 2, b, "123"] 
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        assertSame(1, evaluator.evalCode("a[0]"))
        assertSame(2, evaluator.evalCode("a[1]"))
        assertSame(3, evaluator.evalCode("a[2][0]"))
        assertSame(4, evaluator.evalCode("a[2][1]"))
        assertSame(5, evaluator.evalCode("a[2][2]"))
        assertTrue("123" == evaluator.evalCode("a[3]"))
    }

    @Test
    fun array_setter() {
        val s = """
a = [1, 2, 3] 
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        assertSame(10, evaluator.evalCode("a[0] = a[0] * 10"))
        assertSame(2, evaluator.evalCode("a[1]"))
        assertSame(3, evaluator.evalCode("a[2]"))

        assertSame(20, evaluator.evalCode("a[1] = a[1] * 10"))
        assertSame(10, evaluator.evalCode("a[0]"))
        assertSame(3, evaluator.evalCode("a[2]"))

        assertSame(30, evaluator.evalCode("a[2] = a[2] * 10"))
        assertSame(10, evaluator.evalCode("a[0]"))
        assertSame(20, evaluator.evalCode("a[1]"))
    }


    @Test
    fun arrayOfArray_setter() {
        val s = """
b = [2, 3]
a = [1, b, 4] 
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)
        assertSame(10, evaluator.evalCode("a[0] = a[0] * 10"))
        assertSame(2, evaluator.evalCode("a[1][0]"))
        assertSame(3, evaluator.evalCode("a[1][1]"))
        assertSame(4, evaluator.evalCode("a[2]"))

        //
        assertSame(20, evaluator.evalCode("a[1][0] = a[1][0] * 10"))
        assertSame(10, evaluator.evalCode("a[0]"))
        assertSame(20, evaluator.evalCode("a[1][0]"))
        assertSame(3, evaluator.evalCode("a[1][1]"))
        assertSame(4, evaluator.evalCode("a[2]"))

        //
        assertSame(30, evaluator.evalCode("a[1][1] = a[1][1] * 10"))
        assertSame(10, evaluator.evalCode("a[0]"))
        assertSame(20, evaluator.evalCode("a[1][0]"))
        assertSame(30, evaluator.evalCode("a[1][1]"))
        assertSame(4, evaluator.evalCode("a[2]"))

        //
        assertSame(40, evaluator.evalCode("a[2] = a[2] * 10"))
        assertSame(10, evaluator.evalCode("a[0]"))
        assertSame(20, evaluator.evalCode("a[1][0]"))
        assertSame(30, evaluator.evalCode("a[1][1]"))
        assertSame(40, evaluator.evalCode("a[2]"))
    }

    @Test
    fun arrayOfClass() {
        val s = """
class A {
    a = 0
    def inc() { a = a + 1 }
}
arr = [0, A(), A()]
        """.trimIndent()

        val evaluator = Evaluator()
        evaluator.evalCode(s)

        evaluator.evalCode("arr[0] = A()")
        assertSame(0, evaluator.evalCode("arr[0].a"))
        assertSame(1, evaluator.evalCode("arr[0].inc()"))

        assertSame(1, evaluator.evalCode("arr[0].a"))
        assertSame(0, evaluator.evalCode("arr[1].a"))
        assertSame(0, evaluator.evalCode("arr[2].a"))

        assertSame(1, evaluator.evalCode("arr[1].inc()"))
        assertSame(1, evaluator.evalCode("arr[0].a"))
        assertSame(1, evaluator.evalCode("arr[1].a"))
        assertSame(0, evaluator.evalCode("arr[2].a"))

        assertSame(1, evaluator.evalCode("arr[2].inc()"))
        assertSame(1, evaluator.evalCode("arr[0].a"))
        assertSame(1, evaluator.evalCode("arr[1].a"))
        assertSame(1, evaluator.evalCode("arr[2].a"))

    }
}