package eval

import ast.*

class Function {

    constructor(def: FuncDef, env: Environment) {
        body = def.body
        params = def.params
        this.env = env
    }

    constructor(def: FuncLiteralDef, env: Environment) {
        body = def.body
        params = def.params
        this.env = env
    }

    val body: BlockStatement

    val params: List<Name>

    val env: Environment
}