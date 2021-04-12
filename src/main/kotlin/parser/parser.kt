package parser

import Lexer
import Token
import ast.*

// BNF
// primary: "(" expr ")" | NUMBER | IDENTIFIER | STRING
// factor:  "-" primary | primary
// expr:    factor { OP factor }
// block:   "{" [ statement ] {(";" | EOL) [ statement ]} "}"
// simple:  expr
// statement:   "if" expr block [ "else" block ]
//              | "while" expr block
//              | simple
// program: [ statement ] (";" | EOL)

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
        ";", "}", Token.EOL -> true
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
    if (text == "(") return true
    if (t.isNumber()) return true
    if (t.isString()) return true
    if (t.isIdentifier()) {
        if (isReservedCharacter(t) == false) {
            return true
        }
    }
    return false
}

fun getOperatorPrecedence(op: String): Int {
    return when (op) {
        "=" -> 1
        "==", ">", "<" -> 2
        "+", "-" -> 3
        "*", "/", "%" -> 4
        else -> -1
    }
}

fun isLeftJoinOperator(op: String): Boolean {
    if (op == "=") {
        return false
    }
    return true
}

// (演算子op < op以降の演算子) が成り立つうちは以降の演算子の式を結合する
fun combineBinaryExpr(left: ASTree, op: Token, opPrec: Int, l: Lexer): ASTree {
    var right = parseFactor(l)

    while (true) {
        // 次のトークンが演算子か
        val nextOpToken = l.peek(0)
        val nextOpPrec = getOperatorPrecedence(nextOpToken.getText())

        // 引数の演算子が優先か
        val priorArgOp = when {
            nextOpPrec < 0 -> true
            opPrec > nextOpPrec -> true
            opPrec < nextOpPrec -> false
            else -> {
                if (op.getText() == nextOpToken.getText())
                    isLeftJoinOperator(op.getText())
                else
                    true
            }
        }

        if (priorArgOp) {
            break
        } else {
            // 以降の演算子を優先する場合は右辺の結合を続ける
            l.read()
            right = combineBinaryExpr(right, nextOpToken, nextOpPrec, l)
        }
    }

    return BinaryExpr(left, ASTLeaf(op), right)
}

fun parseProgram(l: Lexer): ASTree {
    val t = l.peek(0)
    val statement = if (isStatementBegining(t)) {
        parseStatement(l)
    } else {
        NullStatement()
    }

    if (readIf(l, ";", Token.EOL) != null) {
        return statement
    }

    throw parseError(l.read())
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
    var left = parseFactor(l)

    while (true) {
        val t = l.peek(0)
        val prec = getOperatorPrecedence(t.getText())
        if (prec < 0) {
            break
        }
        l.read()
        left = combineBinaryExpr(left, t, prec, l)
    }

    return left
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
    val t = l.read()

    if (t.isNumber()) {
        return NumberLiteral(t)
    }

    if (t.isString()) {
        return StringLiteral(t)
    }

    if (t.getText() == "(") {
        val expr = parseExpr(l)
        if (readIf(l, ")") != null) {
            return expr
        }

        throw parseError(l.read())
    }

    if (t.isIdentifier()) {
        if (isReservedCharacter(t) == false) {
            return Name(t)
        }
    }

    throw parseError(t)
}

