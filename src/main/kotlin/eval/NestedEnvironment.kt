package eval

class NestedEnvironment(private var outer: Environment): Environment {

    private val env = BasicEnvironment()

    fun setOuter(outer: Environment)  {
        this.outer = outer
    }

    fun putNew(name: String, value: Any?) {
        env.put(name, value)
    }

    override fun put(name: String, value: Any?) {
        if (env.has(name)) {
            env.put(name, value)
            return
        }

        if (outer.has(name)) {
            outer.put(name, value)
        } else {
            env.put(name, value)
        }
    }

    override fun get(name: String): Any? {
        if (env.has(name)) {
            return env.get(name)
        }
        return outer.get(name)
    }

    override fun has(name: String): Boolean {
        if (env.has(name)) {
            return true
        }
        return outer.has(name)
    }


}