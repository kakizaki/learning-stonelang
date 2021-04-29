package eval

import ast.ClassDef

class StoneObject {
    val def: ClassDef
    private val baseObject: StoneObject?
    private val objectEnv: NestedEnvironment

    constructor(
        def: ClassDef,
        parentEnv: Environment
    ) {
        this.def = def
        this.baseObject = null
        this.objectEnv = NestedEnvironment(parentEnv)
        registerMember()
    }

    constructor(
        def: ClassDef,
        baseObject: StoneObject
    ) {
        this.def = def
        this.baseObject = baseObject
        this.objectEnv = NestedEnvironment(baseObject.getEnv())
        registerMember()
    }

    private fun registerMember() {
        registerVariables()
        registerFunctions()
    }

    private fun registerVariables() {
        for (n in this.def.variables) {
            val name = n.name.name()
            if (isBaseVariable(name) == false) {
                objectEnv.putNew(name, null)
            }
        }
        objectEnv.putNew("base", baseObject)
        objectEnv.putNew("this", this)
    }

    private fun registerFunctions() {
        for (n in this.def.functions) {
            objectEnv.putNew(n.funcName.name(), Function(n, objectEnv))
        }
    }

    fun isBaseVariable(name: String): Boolean {
        if (baseObject == null) {
            return false
        }
        if (baseObject.isBaseVariable(name)) {
            return true
        }
        return baseObject.def.variables.find { it.name.name() == name } != null
    }

    fun set(name: String, value: Any?): Any? {
        if (objectEnv.has(name)) {
            objectEnv.putNew(name, value)
            return value
        }

        if (baseObject != null) {
            return baseObject.set(name, value)
        }
        return null
    }

    fun get(name: String): Any? {
        if (objectEnv.has(name)) {
            return objectEnv.get(name)
        }

        if (baseObject != null) {
            return baseObject.get(name)
        }
        return null
    }

    fun getEnv(): Environment {
        return objectEnv
    }
}