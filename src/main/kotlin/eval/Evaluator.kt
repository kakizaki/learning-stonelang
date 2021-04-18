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

    private val globalEnv = BasicEnvironment()

    private var env: Environment = globalEnv

    init {
        Natives.registerToEnvironment(globalEnv)
    }

    fun eval(t: ASTree): Any? {
        when (t) {
            is Name -> { return env.get(t.name()) }
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
                return evalT(t)
            }
            is NullStatement -> {
                return null
            }
            is BlockStatement -> {
                return evalT(t)
            }
            is IfStatement -> {
                return evalT(t)
            }
            is WhileStatement -> {
                return evalT(t)
            }
            is FuncDef -> {
                return evalT(t)
            }
            is FuncCall -> {
                return evalT(t)
            }
            is FuncLiteralDef -> {
                return evalT(t)
            }
        }

        throw Exception("cannot eval: ${t.javaClass.simpleName} : $t")
    }


    private fun evalT(t: BinaryExpr): Any? {
        val op = t.operator()
        if (op == "=") {
            return computeAssign(t)
        }
        val lv = eval(t.left())
        val rv = eval(t.right())
        return computeOp(lv, op, rv)
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

    private fun evalT(t: BlockStatement): Any? {
        var r: Any? = null
        for (n in t) {
            if (n is NullStatement) {
                continue
            }
            r = eval(n)
        }
        return r
    }

    private fun evalT(t: IfStatement): Any? {
        val c = eval(t.condition())
        if (c as? Int == Evaluator.TRUE) {
            return eval(t.thenBlock())
        }
        t.elseBlock()?.let {
            return eval(it)
        }
        return 0
    }

    private fun evalT(t: WhileStatement): Any? {
        var r: Any? = null
        while (true) {
            val c = eval(t.condition())
            if (c as? Int == Evaluator.TRUE) {
                r = eval(t.body())
            } else {
                break
            }
        }
        return r
    }

    private fun evalT(t: FuncDef): Any? {
        val f = Function(t, env)
        val name = t.funcName.name()
        env.put(name, f)
        return name
    }

    private fun evalT(t: FuncLiteralDef): Any? {
        val f = Function(t, env)
        return f
    }

    private fun evalT(t: FuncCall): Any? {
        val name = t.funcName.name()
        val f = env.get(name)
        if (f is NativeKotlinFunction) {
            if (f.numParams != t.args.size) {
                throw Exception("cannot call native method: need ${f.numParams} arguments, but ${t.args.size}")
            }
            val list = mutableListOf<Any?>()
            for ((i, n) in t.args.withIndex()) {
                val v = eval(n)
                list.add(v)
            }
            return f.invoke(list, t)
        }

        if (f is Function) {
            val nestedEnv = NestedEnvironment(f.env)
            for ((i, n) in t.args.withIndex()) {
                val v = eval(n)
                if (i < f.params.size) {
                    nestedEnv.putNew(f.params[i].name(), v)
                }
            }

            return switchEnv(nestedEnv) {
                eval(f.body)
            }
        }

        throw Exception("undefined function $name")
    }

    private fun switchEnv(newEnv: Environment, block: () -> Any?): Any? {
        val currentEnv = env
        env = newEnv
        val v = block()
        env = currentEnv
        return v
    }

}



