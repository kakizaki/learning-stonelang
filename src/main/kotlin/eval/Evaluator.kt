package eval

import Token
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

    private val functions = mutableMapOf<String, FuncDef>()

    private val classes = mutableMapOf<String, ClassDef>()



    private var env: Environment = globalEnv

    init {
        Natives.registerToEnvironment(globalEnv)
    }

    fun eval(t: ASTree): Any? {
        when (t) {
            is Name -> { return env.get(t.name()) }
            is NumberLiteral -> return t.value()
            is StringLiteral -> return t.value()
            is NegativeExpr -> {
                val v = eval(t.operand())
                if (v is Int) {
                    return -v
                }
                throw Exception("bad type for -")
            }
            is BinaryExpr -> return evalT(t)
            is NullStatement -> return null
            is BlockStatement -> return evalT(t)
            is IfStatement -> return evalT(t)
            is WhileStatement -> return evalT(t)
            is FuncDef -> return evalT(t)
            is ClassDef -> return evalT(t)
            is FuncCall -> return evalT(t)
            is FuncLiteralDef -> return evalT(t)
            is ObjectMember -> return evalT(t)
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
        // TODO this, base 等への代入の禁止
        when (val l = t.left()) {
            is Name -> {
                val v = eval(t.right())
                env.put(l.name(), v)
                return v
            }
            is ObjectMember -> {
                val o = env.get(l.objName.name())
                if (o is StoneObject) {
                    val v = eval(t.right())
                    return o.set(l.memberName.name(), v)
                }
                throw Exception("bad assignment: ${l.objName.name()} is not ${StoneObject::class.simpleName}")
            }
        }

        throw Exception("bad assignment")
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
        // TODO parameter overload
        // TODO overwrite same name function
        val name = t.funcName.name()
        if (globalEnv.find(name)) {
            throw Exception("'${name}' has defined.")
        }
        globalEnv.put(name, Function(t, globalEnv))
        return name
    }

    private fun evalT(t: ClassDef): Any? {
        // TODO extends
        // TODO overwrite same name class
        val name = t.className.name()
        if (globalEnv.find(name)) {
            throw Exception("'${name}' has defined.")
        }
        globalEnv.put(name, t)
        return name
    }

    private fun evalT(t: FuncLiteralDef): Any? {
        return Function(t, env)
    }

    private fun evalT(t: FuncCall): Any? {
        val obj = t.callee as? Name
        if (obj != null) {
            val f = env.get(obj.name())
            when (f) {
                is NativeKotlinFunction -> return call(t, f)
                is Function -> return call(t, f)
                is ClassDef -> return instantiate(f)
            }
            throw Exception("undefined function $f")
        }

        val member = t.callee as? ObjectMember
        if (member != null) {
            val m = evalT(member)
            if (m is Function) {
                return call(t, m)
            }
            throw Exception("undefined function $member")
        }

        throw Exception("undefined function ${t.callee}")
    }

    private fun evalT(t: ObjectMember): Any? {
        val obj = env.get(t.objName.name())
        if (obj is StoneObject) {
            return obj.get(t.memberName.name())
        }

        throw Exception("undefined object ${t.objName.name()}")
    }

    private fun call(t: FuncCall, f: NativeKotlinFunction): Any? {
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

    private fun call(t: FuncCall, f: Function): Any? {
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

    private fun instantiate(c: ClassDef, baseObject: StoneObject? = null): StoneObject {
        var b = baseObject
        if (c.baseClassName != null) {
            val base = globalEnv.get(c.baseClassName.name()) as? ClassDef
            if (base == null) {
                throw Exception("base class '${c.baseClassName.name()}' is undefined or is not class.")
            }
            b = instantiate(base, baseObject)
        }

        val i = if (b != null) StoneObject(c, b) else StoneObject(c, globalEnv)
        switchEnv(i.getEnv()) {
            for (n in c.variables) {
                val v = eval(n.value)
                i.set(n.name.name(), v)
            }
        }
        return i
    }

    private fun switchEnv(newEnv: Environment, block: () -> Any?): Any? {
        val currentEnv = env
        env = newEnv
        val v = block()
        env = currentEnv
        return v
    }

}


