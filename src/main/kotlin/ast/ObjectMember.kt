package ast

class ObjectMember(
    val callee: ASTree,
    val memberName: Name
): ASTList(listOf(callee, memberName)) {

    override fun toString(): String {
        return "($callee.$memberName)"
    }
}