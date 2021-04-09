package eval

import ast.*

class Evaluator {
    companion object {
        const val TRUE = 1
        const val FALSE = 0

        fun Boolean.constValue(): Int {
            return if (this) TRUE else FALSE
        }
    }

    val env = BasicEnvironment()

    fun eval(t: ASTree): Any? {
        when (t) {
            is Name -> {
                return env.get(t.name())
            }
            is NumberLiteral -> {
                return t.value()
            }
            is StringLiteral -> {
                return t.value()
            }
            is NegativeExpr -> {
                val v = eval(t.operand())
                if (v is Int) {
                    return -v
                }
                throw Exception("bad type for -")
            }
            is BinaryExpr -> {
                val op = t.operator()
                if (op == "=") {
                    return computeAssign(t)
                }
                val lv = eval(t.left())
                val rv = eval(t.right())
                return computeOp(lv, op, rv)
            }
            is NullStatement -> {
                return null
            }
            is BlockStatement -> {
                var r: Any? = null
                for (n in t) {
                    if (n is NullStatement) {
                        continue
                    }
                    r = eval(n)
                }
                return r
            }
            is IfStatement -> {
                val c = eval(t.condition())
                if (c as? Int == TRUE) {
                    return eval(t.thenBlock())
                }
                t.elseBlock()?.let {
                    return eval(it)
                }
                return 0
            }
            is WhileStatement -> {
                var r: Any? = null
                while (true) {
                    val c = eval(t.condition())
                    if (c as? Int == TRUE) {
                        r = eval(t.body())
                    } else {
                        break
                    }
                }
                return r
            }
        }

        throw Exception("cannot eval: ${t.javaClass.simpleName} : $t")
    }

    private fun computeAssign(t: BinaryExpr): Any? {
        val name = t.left() as? Name
        if (name == null) {
            throw Exception("bad assignment")
        }
        val v = eval(t.right())
        env.put(name.name(), v)
        return v
    }

    private fun computeOp(lv: Any?, op: String, rv: Any?): Any? {
        if (lv is Int && rv is Int) {
            return computeNumber(lv, op, rv)
        }
        if (op == "+") {
            return lv.toString() + rv.toString()
        }
        if (op == "==") {
            if (lv == null) {
                return (rv == null).constValue()
            } else {
                return (lv == rv).constValue()
            }
        }
        throw Exception("bad type")
    }

    private fun computeNumber(lv: Int, op: String, rv: Int): Int {
        return when (op) {
            "+" -> lv + rv
            "-" -> lv - rv
            "*" -> lv * rv
            "/" -> lv / rv
            "%" -> lv % rv
            "<" -> (lv < rv).constValue()
            ">" -> (lv > rv).constValue()
            "==" -> (lv == rv).constValue()
            else -> {
                throw Exception("bad operator")
            }
        }
    }
}