import Util.evalCode
import Util.evalCodeError
import eval.Evaluator
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertSame



class TestClass {

    @Test
    fun memberVariable() {
        val evaluator = Evaluator()
        val s = """
class classA {
  b = 2
}
        """.trimIndent()

        evaluator.evalCode(s)
        evaluator.evalCode("A = classA()")
        assertSame(2, evaluator.evalCode("A.b"))
        assertSame(3, evaluator.evalCode("A.b = 3"))
        assertSame(null, evaluator.evalCode("b"))
        assertSame(null, evaluator.evalCode("A.a"))
        assertSame(null, evaluator.evalCode("A.a = 2"))
        assertSame(null, evaluator.evalCode("A.a"))
    }

    @Test
    fun memberFunction() {
        val evaluator = Evaluator()

        val s = """
class classA {
  n = 12
  
  def fib(n) {
    if (n < 2) {
        1
    } else {
        fib(n-1) + fib(n-2)
    }
  }
}
        """.trimIndent()

        evaluator.evalCode( s)
        evaluator.evalCode("A = classA()")
        assertSame(1, evaluator.evalCode("A.fib(0)"))
        assertSame(1, evaluator.evalCode("A.fib(1)"))
        assertSame(2, evaluator.evalCode("A.fib(2)"))
        assertSame(3, evaluator.evalCode("A.fib(3)"))
        assertSame(5, evaluator.evalCode("A.fib(4)"))
        assertSame(8, evaluator.evalCode("A.fib(5)"))
        assertSame(12, evaluator.evalCode("A.n"))
        assertNotNull(evaluator.evalCodeError("A.aa()"))
    }

    @Test
    fun globalVariable() {
        val evaluator = Evaluator()

        val s = """
a = 1
class classA {
  b = 2

  def addA() {
    a + b
  }

  def argA(a) {
    a = a
  }
 
  def setA() {
    a = b
  }
}
        """.trimIndent()

        evaluator.evalCode(s)
        evaluator.evalCode("A = classA()")
        assertSame(3, evaluator.evalCode("A.addA()"))
        assertSame(1, evaluator.evalCode("a"))

        assertSame(12, evaluator.evalCode("A.argA(12)"))
        assertSame(1, evaluator.evalCode("a"))

        assertSame(2, evaluator.evalCode("A.setA()"))
        assertSame(2, evaluator.evalCode("a"))
    }


    @Test
    fun extends() {
        val s = """
a = 12
class classA {
  a = 1
  def getB() { b }
  def getC() { c }
} 
class classB extends classA {
  b = 2
  c = 3
  def add() {
    b = b + 1
    a + b
  }
  
  def getB() { b }
  def getBaseB() { base.getB() }
  def getThisB() { this.getB() }
  
  def getBaseA() { base.a }
  def getThisA() { this.a }
  
  def getBaseC() { base.getC() }
  def getThisC() { this.getC() }
  def getThisC2() { getC() }
}
        """.trimIndent()

        val e = Evaluator()
        e.evalCode(s)
        e.evalCode("A = classA()")
        assertSame(null, e.evalCode("A.getB()"))

        e.evalCode( "B = classB()")

        // base.getB() は base オブジェクトのメンバへアクセスする. base.b は未定義なので null のはず.
        assertSame(2, e.evalCode("B.getB()"))
        assertSame(null, e.evalCode("B.getBaseB()"))
        assertSame(2, e.evalCode("B.getThisB()"))

        //
        assertSame(4, e.evalCode("B.add()"))
        assertSame(3, e.evalCode("B.getB()"))
        assertSame(null, e.evalCode("B.getBaseB()"))
        assertSame(3, e.evalCode("B.getThisB()"))

        // base.a も this.a も同じ値のはず.
        assertSame(11, e.evalCode("B.a = 11"))
        assertSame(11, e.evalCode("B.getBaseA()"))
        assertSame(11, e.evalCode("B.getThisA()"))

        // グローバルに c を定義する. classA は グローバルの c へアクセスするはず.
        assertSame(123, e.evalCode("c = 123"))
        assertSame(123, e.evalCode("A.getC()"))
        assertSame(123, e.evalCode("B.getBaseC()"))
        assertSame(123, e.evalCode("B.getThisC()"))
        assertSame(123, e.evalCode("B.getThisC2()"))
    }

    @Test
    fun extends_2() {
        val s = """
a = 12
class classA {
  a = 1
  def getB() { b }
  def getC() { c }
} 
class classB extends classA {
  b = 2
  def getBaseB() { base.getB() }
  def getThisB() { this.getB() }
}
class classC extends classB {
  c = 3
  def base_getBaseB() { base.getBaseB() }
}
        """.trimIndent()

        val e = Evaluator()
        e.evalCode(s)
        e.evalCode("o = classC()")
        assertSame(3, e.evalCode("o.c"))
        assertSame(2, e.evalCode("o.b"))
        assertSame(1, e.evalCode("o.a"))

        assertSame(null, e.evalCode("o.getB()"))
        assertSame(null, e.evalCode("o.getC()"))

        //
        assertSame(null, e.evalCode("o.getBaseB()"))
        assertSame(null, e.evalCode("o.base_getBaseB()"))
    }
}