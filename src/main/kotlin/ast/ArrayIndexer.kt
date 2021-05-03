package ast

class ArrayIndexer: ASTList {
    constructor(
        callee: ASTree,
        indexer: ASTree
    ) : super(listOf(callee, indexer)) {
        this.callee = callee
        this.indexer = indexer
    }

    val callee: ASTree
    val indexer: ASTree

    override fun toString(): String {
        return "($callee [$indexer])"
    }
}