import Util.evalCode
import Util.evalCodeError
import eval.Evaluator
import junit.framework.Assert.assertNotNull
import kotlin.test.Test
import kotlin.test.assertSame

class TestPrimaryPostfix {

    @Test
    fun property_undefinedObject() {
        val evaluator = Evaluator()
        val s = """
class A {
    a = 1
    def m() { a + 1 }
}
aaa = A()
        """.trimIndent()

        evaluator.evalCode(s)
        assertSame(1, evaluator.evalCode("aaa.a"))
        assertSame(2, evaluator.evalCode("aaa.m()"))

        // 文字列に対しての呼び出し
        assertNotNull(evaluator.evalCodeError("\"aaa\".a"))
        assertNotNull(evaluator.evalCodeError("\"aaa\".m()"))

        // 数値に対しての呼び出し
        assertNotNull(evaluator.evalCodeError("12.a"))
        assertNotNull(evaluator.evalCodeError("12.m()"))

        // 宣言していないオブジェクトへの呼び出し
        assertNotNull(evaluator.evalCodeError("bbb.a"))
        assertNotNull(evaluator.evalCodeError("bbb.m()"))
    }


    @Test
    fun objectMember() {
        val evaluator = Evaluator()
        val s = """
class classA {
  a = 1
  def m() { a = a + 1 }
}

class classB {
  a = 11
  A = classA()
  A2 = classA()
  
  def m() { a = a + 1 }
  def getA2() { A2 }
}
        """.trimIndent()

        evaluator.evalCode(s)
        evaluator.evalCode("o = classB()")

        // プロパティへのアクセス. getter.
        assertSame(11, evaluator.evalCode("o.a"))
        assertSame(1, evaluator.evalCode("o.A.a"))

        // プロパティへのアクセス. setter.
        assertSame(5, evaluator.evalCode("o.a = 5"))
        assertSame(5, evaluator.evalCode("o.a"))
        assertSame(1, evaluator.evalCode("o.A.a"))

        assertSame(15, evaluator.evalCode("o.A.a = 15"))
        assertSame(5, evaluator.evalCode("o.a"))
        assertSame(15, evaluator.evalCode("o.A.a"))

        // メソッドの呼び出し
        assertSame(6, evaluator.evalCode("o.m()"))
        assertSame(6, evaluator.evalCode("o.a"))
        assertSame(15, evaluator.evalCode("o.A.a"))

        assertSame(16, evaluator.evalCode("o.A.m()"))
        assertSame(6, evaluator.evalCode("o.a"))
        assertSame(16, evaluator.evalCode("o.A.a"))

        // A2 を変更してみる
        assertSame(1, evaluator.evalCode("o.getA2().a"))
        assertSame(2, evaluator.evalCode("o.getA2().m()"))
        assertSame(2, evaluator.evalCode("o.getA2().a"))
        assertSame(2, evaluator.evalCode("o.A2.a"))

        assertSame(25, evaluator.evalCode("o.A2.a = 25"))
        assertSame(26, evaluator.evalCode("o.getA2().m()"))
        assertSame(26, evaluator.evalCode("o.getA2().a"))
        assertSame(26, evaluator.evalCode("o.A2.a"))

        // A2 以外は変わっていないはず
        assertSame(6, evaluator.evalCode("o.a"))
        assertSame(16, evaluator.evalCode("o.A.a"))
    }
}