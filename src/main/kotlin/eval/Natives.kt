package eval

import StoneException
import java.lang.NumberFormatException
import javax.swing.JOptionPane
import kotlin.reflect.KCallable

object Natives {
    fun registerToEnvironment(env: Environment) {
        register(env, "print", Natives::print)
        register(env, "read", Natives::read)
        register(env, "toInt", Natives::toInt)
        register(env, "currentTime", Natives::currentTime)
        register(env, "length", String::length)
    }

    private fun register(
        env: Environment,
        name: String,
        clazz: Class<*>,
        methodName: String,
        vararg params: Class<*>
    ) {
        try {
            val m = clazz.getMethod(methodName, *params)
            val f = NativeFunction(methodName, m)
            env.put(name, f)
        } catch (e: Exception) {
            throw StoneException("cannot find a native function: $methodName")
        }
    }

    private fun register(
        env: Environment,
        name: String,
        callable: KCallable<*>
    ) {
        val f = NativeKotlinFunction(callable.name, callable)
        env.put(name, f)
    }

    fun print(obj: Any): Int {
        println(obj.toString())
        return 0
    }

    fun read(): String {
        return JOptionPane.showInputDialog(null)
    }

    fun toInt(v: Any): Int {
        when (v) {
            is String -> return v.toInt()
            is Int -> return v
        }
        throw NumberFormatException(v.toString())
    }

    val startTime = System.currentTimeMillis()

    fun currentTime(): Int {
        return (System.currentTimeMillis() - startTime).toInt()
    }
}