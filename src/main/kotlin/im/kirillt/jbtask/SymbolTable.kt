package im.kirillt.jbtask

import im.kirillt.jbtask.AST.Variable

class SymbolTable {
    private val stack = mutableListOf<MutableMap<String, Variable>>()
    fun enterScope() {
        stack.add(mutableMapOf())
    }

    fun findSymbol(name: String): Variable? {
        for (i in stack.reversed())
            if (i.containsKey(name))
                return i[name]
        return null
    }

    fun addSymbol(variable: Variable) {
        stack.last()[variable.name] = variable
    }

    fun isDefinedInCurrentScope(name: String): Boolean {
        if (stack.isEmpty())
            return false
        else
            return stack.last().containsKey(name)
    }

    fun exitScope() {
        stack.dropLast(1)
    }

    fun copy() : SymbolTable { //it's crap, i know
        val other = SymbolTable();
        for (i in stack)
            other.stack.add(i)
        return other
    }
}
