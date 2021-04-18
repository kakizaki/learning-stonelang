package eval

import StoneException
import ast.ASTree
import java.lang.reflect.Method
import kotlin.reflect.KCallable

class NativeFunction(
    private val name: String,
    private val method: Method,
    ) {

    val numParams: Int
        get() {
            return method.parameterCount
        }

    override fun toString(): String {
        return "<native: ${hashCode()}>"
    }

    fun invoke(args: List<Any?>, tree: ASTree): Any?  {
        try {
            return method.invoke(null, args)
        } catch (e: Exception) {
            throw StoneException("bad native function call: $name")
        }
    }
}


class NativeKotlinFunction(
    private val name: String,
    private val method: KCallable<*>,
) {

    val numParams: Int
        get() {
            return method.parameters.size
        }

    override fun toString(): String {
        return "<native: ${hashCode()}>"
    }

    fun invoke(args: List<Any?>, tree: ASTree): Any?  {
        try {
            return method.call(*args.toTypedArray())
        } catch (e: Exception) {
            throw StoneException("bad native function call: $name, $e")
        }
    }
}


