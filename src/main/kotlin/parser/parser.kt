package parser

import Lexer
import Token
import ast.*

object ParseUtil {
    val binaryOperator: BinaryOperatorSet

    init {
        binaryOperator = BinaryOperatorContainer().apply {
            add("=", 1, false)
            add("==", 2, true)
            add("<", 2, true)
            add(">", 2, true)
            add("+", 3, true)
            add("-", 3, true)
            add("*", 4, true)
            add("/", 4, true)
            add("%", 4, true)
        }
    }

}

// BNF
// []: 0..1
// {}: 0..n
// (): 1
//  = = = = = = = = = =
// array-def:           "[" [ expr { "," expr } ] "]"
//  = = = = = = = = = =
// var-def:             IDENTIFIER [ "=" factor ]
// class-member:        func-def | var-def
// class-body:          "{" [ class-member ] { (";" | EOL) [ class-member ] } "}"
// class-def:           "class" IDENTIFIER [ "extends" IDENTIFIER ] class-body
//  = = = = = = = = = =
// param-list:          IDENTIFIER { "," IDENTIFIER }
// func-def:            "def" IDENTIFIER "(" [ param-list ] ")" block
// closure-def:         "def" "(" [ param-list ] ")" block
//  = = = = = = = = = =
// primary-postfix:     ( --fun-call | --array-indexer | --class-property )
// --func-call:         "(" [ expr { "," expr } ] ")"
// --member:            "." IDENTIFIER
// --array-indexer:     "[" expr "]"
// primary:             (
//                      "(" expr ")"
//                      | NUMBER
//                      | STRING
//                      | IDENTIFIER
//                      | closure-def
//                      | array
//                      )
//                      { primary-postfix }

// factor:      "-" primary | primary
// expr:        factor { OP factor }
// block:       "{" [ statement ] { (";" | EOL) [ statement ] } "}"
// simple:      expr
// statement:   "if" expr block [ "else" block ]
//              | "while" expr block
//              | simple
// program:     [ func-def | class-def | statement ] (";" | EOL)





fun parseError(t: Token): Exception {
    return Exception("parse error ${t.getText()}")
}

fun parseError(l: Lexer): Exception {
    return parseError(l.read())
}

fun readIf(l: Lexer, vararg s: String): Token? {
    return readIf(l) block@ {
        for (n in s) {
            if (it == n) {
                return@block true
            }
        }
        return@block false
    }
}

inline fun readIf(l: Lexer, cond:(String) -> Boolean): Token? {
    val t = l.peek(0)
    if (t.isIdentifier() && cond(t.getText())) {
        return l.read()
    }
    return null
}

fun isReservedIdentifier(t: Token): Boolean {
    if (t.isIdentifier() == false) {
        return false
    }

    return when (t.getText()) {
        ";", Token.EOL,
        "{", "}", "(", ")",
        "while", "if", "else",
        "def", "class", "extends", -> true
        else -> false
    }
}

fun isPrimaryBeginning(t: Token): Boolean {
    if (t.isNumber()) return true
    if (t.isString()) return true
    if (t.isIdentifier()) {
        val text = t.getText()

        // expr
        if (text == "(") return true

        // closure
        if (text == "def") return true

        if (isReservedIdentifier(t) == false) {
            return true
        }
    }
    return false
}

fun isStatementBeginning(t: Token): Boolean {
    val text = t.getText()

    // statement
    if (t.isIdentifier()) {
        if (text == "if") return true
        if (text == "while") return true
    }

    // factor
    if (t.isIdentifier()) {
        if (text == "-") return true
    }

    // primary
    if (isPrimaryBeginning(t)) return true

    return false
}

fun isFuncDefBeginning(t: Token): Boolean {
    if (t.isIdentifier() && t.getText() == "def") {
        return true
    }
    return false
}

fun isClassDefBeginning(t: Token): Boolean {
    if (t.isIdentifier() && t.getText() == "class") {
        return true
    }
    return false
}


fun parseProgram(l: Lexer): ASTree {
    val t = l.peek(0)

    val tree = when {
        isFuncDefBeginning(t) -> parseFuncDef(l)
        isClassDefBeginning(t) -> parseClassDef(l)
        isStatementBeginning(t) -> parseStatement(l)
        else -> NullStatement()
    }

    if (readIf(l, ";", Token.EOL) != null) {
        return tree
    }

    throw parseError(l.read())
}

fun parseFuncDef(l: Lexer): FuncDef {
    if (readIf(l, "def") == null) {
        throw parseError(l.read())
    }

    val defName = l.read()
    if (defName.isIdentifier() == false) {
        throw parseError(defName)
    }
    if (isReservedIdentifier(defName)) {
        throw parseError(defName)
    }

    if (readIf(l, "(") == null) {
        throw parseError(l.read())
    }

    val params = mutableListOf<Name>()
    if (readIf(l, ")") == null) {
        while (true) {
            val p = l.read()
            if (p.isIdentifier() && isReservedIdentifier(p) == false) {
                params.add(Name(p))
            } else {
                throw parseError(p)
            }
            if (readIf(l, ")") != null) {
                break
            }
            if (readIf(l, ",") == null) {
                throw parseError(p)
            }
        }
    }

    val body = parseBlock(l)
    return FuncDef(Name(defName), params, body)
}

fun parseVarDef(l: Lexer): VarDef {
    val t = l.read()
    if (t.isIdentifier() == false) {
        throw parseError(t)
    }
    if (isReservedIdentifier(t)) {
        throw parseError(t)
    }
    val v = if (readIf(l, "=") == null)  {
        NullStatement()
    } else {
        parseFactor(l)
    }
    return VarDef(Name(t), v)
}

fun parseClassMember(l: Lexer): ASTree {
    val t = l.peek(0)
    if (isFuncDefBeginning(t)) {
        return parseFuncDef(l)
    }
    return parseVarDef(l)
}

fun parseClassBody(l: Lexer): Pair<List<VarDef>, List<FuncDef>> {
    val varList = mutableListOf<VarDef>()
    val funcList = mutableListOf<FuncDef>()
    for (n in parseLines(l) { parseClassMember(l) }) {
        when (n) {
            is VarDef -> varList.add(n)
            is FuncDef -> funcList.add(n)
            else -> throw parseError(l)
        }
    }
    return Pair(varList, funcList)
}

fun parseClassDef(l: Lexer): ClassDef {
    if (readIf(l, "class") == null) {
        throw parseError(l.read())
    }

    val className = l.read()
    if (isReservedIdentifier(className)) {
        throw parseError(className)
    }

    val baseClass = if (readIf(l, "extends") == null) {
        null
    } else {
        l.read().apply {
            if (isReservedIdentifier(this)) {
                throw parseError(this)
            }
        }
    }

    val (varList, funcList) = parseClassBody(l)
    return if (baseClass == null) {
        ClassDef(Name(className), varList, funcList)
    } else {
        ClassDef(Name(className), Name(baseClass), varList, funcList)
    }
}

fun parseFuncLiteralDef(l: Lexer): FuncLiteralDef {
    if (readIf(l, "def") == null) {
        throw parseError(l.read())
    }

    if (readIf(l, "(") == null) {
        throw parseError(l.read())
    }

    val params = mutableListOf<Name>()
    if (readIf(l, ")") == null) {
        while (true) {
            val p = l.read()
            if (p.isIdentifier() && isReservedIdentifier(p) == false) {
                params.add(Name(p))
            } else {
                throw parseError(p)
            }
            if (readIf(l, ")") != null) {
                break
            }
            if (readIf(l, ",") == null) {
                throw parseError(p)
            }
        }
    }

    val body = parseBlock(l)
    return FuncLiteralDef(params, body)
}

fun parseStatement(l: Lexer): ASTree {
    if (readIf(l, "if") != null) {
        val expr = parseExpr(l)
        val thenBlock = parseBlock(l)

        if (readIf(l, "else") != null) {
            val elseBlock = parseBlock(l)
            return IfStatement(expr, thenBlock, elseBlock)
        } else {
            return IfStatement(expr, thenBlock)
        }
    }

    if (readIf(l, "while") != null) {
        val expr = parseExpr(l)
        val thenBlock = parseBlock(l)
        return WhileStatement(expr, thenBlock)
    }

    return parseSimple(l)
}

fun parseSimple(l: Lexer): ASTree {
    return parseExpr(l)
}


// 以下のような BNF のパターンをパースする
// BNF:: "{" parsedObject { ( ";" | EOL ) [ parsedObject ] } "}"
fun <T> parseLines(l: Lexer, parser: (Lexer) -> T): Sequence<T> {
    return sequence {
        if (readIf(l, "{") == null) {
            throw parseError(l)
        }

        var waitLineBreak = false
        while (readIf(l, "}") == null) {
            if (readIf(l, ";", Token.EOL) != null) {
                waitLineBreak = false
                continue
            }
            if (waitLineBreak) {
                throw parseError(l)
            }
            yield(parser(l))
            waitLineBreak = true
        }
    }
}

// 以下のような BNF のパターンをパースする
// BNF::    openChar [ parsedObject { (splitChars) parsedObject } ] closeChar
// ex. ( )
// ex. ( aaa )
// ex. ( aaa , bbb , ccc )
fun <T> parseSplitToken(
    l: Lexer,
    openChar: String,
    splitChars: Array<String>,
    closeChar: String,
    parser: (Lexer) -> T
): Sequence<T> {
    return sequence {
        if (readIf(l, openChar) == null) {
            throw parseError(l)
        }

        var first = true
        while (readIf(l, closeChar) == null) {
            if (first == false) {
                if (readIf(l, *splitChars) == null) {
                    throw parseError(l)
                }
            }

            yield(parser(l))
            first = false
        }
    }
}



fun parseBlock(l: Lexer): BlockStatement {
    val list = parseLines(l) {
        parseStatement(l)
    }.toList()
    return BlockStatement(list)
}

fun parseExpr(l: Lexer): ASTree {
    val builder = BinaryExprBuilder(ParseUtil.binaryOperator)

    while (true) {
        val f = parseFactor(l)
        val op = l.peek(0)
        if (ParseUtil.binaryOperator.isOperator(op.getText())) {
            l.read()
            builder.next(f, op)
        } else {
            return builder.last(f)
        }
    }
}

fun parseFactor(l: Lexer): ASTree {
    if (readIf(l, "-") != null) {
        val primary = parsePrimary(l)
        return NegativeExpr(listOf(primary))
    } else {
        val primary = parsePrimary(l)
        //return PrimaryExpr(listOf(primary))
        return primary
    }
}

fun parsePrimaryT(l: Lexer): ASTree {
    val t = l.peek(0)

    if (t.isNumber()) {
        l.read()
        return NumberLiteral(t)
    }

    if (t.isString()) {
        l.read()
        return StringLiteral(t)
    }

    if (t.getText() == "(") {
        l.read()
        val expr = parseExpr(l)
        if (readIf(l, ")") != null) {
            return expr
        }
        throw parseError(l)
    }

    if (t.isIdentifier()) {
        if (t.getText() == "def") {
            return parseFuncLiteralDef(l)
        }
        return Name(l.read())
    }
    throw parseError(l)
}

fun parseFuncArg(l: Lexer): List<ASTree> {
    return parseSplitToken(l, "(", arrayOf(","), ")") {
        parseExpr(l)
    }.toList()
}

fun tryWrapPrimaryPostfix(l: Lexer, target: ASTree): ASTree? {
    val t = l.peek(0)
    if (t.isIdentifier() && t.getText() == ".") {
        l.read()
        val name = l.read().let {
            if (isReservedIdentifier(it)) {
                throw parseError(it)
            }
            if (it.isIdentifier() == false) {
                throw parseError(it)
            }
            Name(it)
        }
        return ObjectMember(target, name)
    }

    if (t.isIdentifier() && t.getText() == "(") {
        val args = parseFuncArg(l)
        return FuncCall(target, args)
    }

    return null
}


fun parsePrimary(l: Lexer): ASTree {
    var p = parsePrimaryT(l)

    while (true) {
        val w = tryWrapPrimaryPostfix(l, p)
        if (w != null) {
            p = w
        } else {
            break
        }
    }
    return p
}


