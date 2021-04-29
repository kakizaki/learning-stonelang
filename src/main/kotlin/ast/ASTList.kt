package ast

open class ASTList(private val list: List<ASTree>) : ASTree() {
    companion object {
        fun toString(list: Sequence<ASTree>): String {
            val b = StringBuilder()
            b.append('(')
            var sep = ""
            for (t in list) {
                b.append(sep)
                sep = " "
                b.append(t.toString())
            }
            b.append(')')
            return b.toString()
        }
    }
    override fun child(i: Int): ASTree {
        return list[i]
    }

    override fun numChildren(): Int {
        return list.size
    }

    override fun children(): Iterator<ASTree> {
        return list.iterator()
    }

    override fun location(): String? {
        for (t in list) {
            val s = t.location()
            if (s != null) {
                return s
            }
        }
        return null
    }

    override fun toString(): String {
        return Companion.toString(list.asSequence())
    }
}