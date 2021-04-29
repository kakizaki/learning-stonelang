package ast

class ClassDef: ASTList {

    constructor(
        className: Name,
        variables: List<VarDef>,
        functions: List<FuncDef>
    ): super(listOf(className, *variables.toTypedArray(), *functions.toTypedArray())) {
        this.className = className
        this.variables = variables
        this.functions = functions

        this.baseClassName = null
    }


    constructor(
        className: Name,
        superClassName: Name,
        variables: List<VarDef>,
        functions: List<FuncDef>
    ): super(listOf(className, superClassName, *variables.toTypedArray(), *functions.toTypedArray())) {
        this.className = className
        this.variables = variables
        this.functions = functions

        this.baseClassName = superClassName
    }

    val className: Name

    val baseClassName: Name?

    val variables: List<VarDef>

    val functions: List<FuncDef>

    override fun toString(): String {
        val base = baseClassName ?: "*"

        val s = variables.asSequence() + functions.asSequence()
        val body = ASTList.Companion.toString(s)
        return "(class $className $base $body)"
    }
}