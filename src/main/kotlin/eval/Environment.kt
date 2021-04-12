package eval

interface Environment {
    fun put(name: String, value: Any?)
    fun get(name: String): Any?
    fun has(name: String): Boolean
}