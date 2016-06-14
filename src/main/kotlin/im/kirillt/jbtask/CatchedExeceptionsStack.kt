package im.kirillt.jbtask

import im.kirillt.jbtask.AST.Class

class CatchedExeceptionsStack {
    private val stack = mutableListOf<MutableMap<String, Class>>()

    fun enterScope() {
        stack.add(mutableMapOf())
    }

    fun isCatched(exception: Class): Boolean {
        for (i in stack.reversed()) {
            for (cls in i.values)
                if (exception.isChildOrSameAs(cls))
                    return true
        }
        return false
    }

    fun addException(exception: Class) {
        stack.last()[exception.name] = exception
    }

    fun exitScope() {
        stack.dropLast(1)
    }
}
