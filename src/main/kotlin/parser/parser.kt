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
// param-list:  IDENTIFIER ( "," IDENTIFIER )
// func-def:    "def" IDENTIFIER "(" [ param-list ] ")" block
// closure-def: "def" "(" [ param-list ] ")" block
// arg-list:    expr ( "," expr )
// func-call:   IDENTIFIER "(" [ arg-list ] ")"

// primary: "(" expr ")" | NUMBER | IDENTIFIER | STRING | func-call | closure-def
// factor:  "-" primary | primary
// expr:    factor { OP factor }
// block:   "{" [ statement ] {(";" | EOL) [ statement ]} "}"
// simple:  expr
// statement:   "if" expr block [ "else" block ]
//              | "while" expr block
//              | simple
// program: [ func-def | statement ] (";" | EOL)

fun parseError(t: Token): Exception {
    return Exception("parse error ${t.getText()}")
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
    val text = l.peek(0).getText()
    if (cond(text)) {
        return l.read()
    }
    return null
}

fun isReservedCharacter(t: Token): Boolean {
    return  when (t.getText()) {
        ";", "}", Token.EOL, ")", "(", "def" -> true
        else -> false
    }
}

fun isStatementBegining(l: Lexer): Boolean {
    return isStatementBegining(l.peek(0))
}

fun isStatementBegining(t: Token): Boolean {
    val text = t.getText()

    // statement
    if (text == "if") return true
    if (text == "while") return true

    // factor
    if (text == "-") return true

    // primary
    if (t.isNumber()) return true
    if (t.isString()) return true
    if (t.isIdentifier()) {
        if (text == "(") return true
        if (text == "def") return true
        if (isReservedCharacter(t) == false) {
            return true
        }
    }
    return false
}

fun isFuncDefBegining(t: Token): Boolean {
    if (t.getText() == "def") {
        return true
    }
    return false
}


fun parseProgram(l: Lexer): ASTree {
    val t = l.peek(0)

    val tree = if (isFuncDefBegining(t)) {
        parseFuncDef(l)
    } else if (isStatementBegining(t)) {
        parseStatement(l)
    } else {
        NullStatement()
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
    if (isReservedCharacter(defName)) {
        throw parseError(defName)
    }

    if (readIf(l, "(") == null) {
        throw parseError(l.read())
    }

    val params = mutableListOf<Name>()
    if (readIf(l, ")") == null) {
        while (true) {
            val p = l.read()
            if (p.isIdentifier() && isReservedCharacter(p) == false) {
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
            if (p.isIdentifier() && isReservedCharacter(p) == false) {
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


fun parseFuncCall(l: Lexer): ASTree {
    val callName = l.read()
    if (callName.isIdentifier() == false) {
        throw parseError(callName)
    }
    if (isReservedCharacter(callName)) {
        throw parseError(callName)
    }

    if (readIf(l, "(") == null) {
        throw parseError(l.read())
    }
    val args = mutableListOf<ASTree>()
    if (readIf(l, ")") != null) {
        while (true) {
            val a = parseExpr(l)
            args.add(a)

            if (readIf(l, ")") != null) {
                break
            }
            if (readIf(l, ",") == null) {
                throw parseError(l.read())
            }
        }
    }

    return FuncCall(Name(callName), args)
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

fun parseBlock(l: Lexer): BlockStatement {
    val list = mutableListOf<ASTree>()

    if (readIf(l, "{") == null) {
        throw parseError(l.read())
    }

    if (isStatementBegining(l)) {
        val statement = parseStatement(l)
        list.add(statement)
    }

    while (true) {
        if (readIf(l, ";", Token.EOL) == null) {
            break
        }

        if (isStatementBegining(l)) {
            val statement = parseStatement(l)
            list.add(statement)
        }
    }

    if (readIf(l, "}") == null) {
        throw parseError(l.read())
    }

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

fun parsePrimary(l: Lexer): ASTree {
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

        throw parseError(l.read())
    }

    if (t.isIdentifier()) {
        if (isReservedCharacter(t) == false) {
            l.read()
            if (readIf(l, "(") != null) {
                val args = mutableListOf<ASTree>()
                if (readIf(l, ")") == null) {
                    while (true) {
                        val a = parseExpr(l)
                        args.add(a)

                        if (readIf(l, ")") != null) {
                            break
                        }
                        if (readIf(l, ",") == null) {
                            throw parseError(l.read())
                        }
                    }
                }
                return FuncCall(Name(t), args)
            }

            return Name(t)
        }

        if (t.getText() == "def") {
            return parseFuncLiteralDef(l)
        }
    }

    throw parseError(t)
}









