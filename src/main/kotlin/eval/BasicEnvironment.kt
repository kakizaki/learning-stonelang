package eval

import eval.Environment

class BasicEnvironment: Environment {
    private val values = mutableMapOf<String, Any?>()

    override fun put(name: String, value: Any?) {
        values[name] = value
    }

    override fun get(name: String): Any? {
        return values[name]
    }

    override fun has(name: String): Boolean {
        return values.containsKey(name)
    }
}