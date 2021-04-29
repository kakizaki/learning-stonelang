package ast

class ObjectMember(
    val objName: Name,
    val memberName: Name
): ASTList(listOf(objName, memberName)) {

    override fun toString(): String {
        return "($objName.$memberName)"
    }
}