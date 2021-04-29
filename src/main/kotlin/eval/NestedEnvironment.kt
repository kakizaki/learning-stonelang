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
        if (env.find(name)) {
            env.put(name, value)
            return
        }

        if (outer.find(name)) {
            outer.put(name, value)
        } else {
            env.put(name, value)
        }
    }

    override fun get(name: String): Any? {
        if (env.find(name)) {
            return env.get(name)
        }
        return outer.get(name)
    }

    override fun find(name: String): Boolean {
        if (env.find(name)) {
            return true
        }
        return outer.find(name)
    }

    fun has(name: String): Boolean {
        return env.find(name)
    }



}