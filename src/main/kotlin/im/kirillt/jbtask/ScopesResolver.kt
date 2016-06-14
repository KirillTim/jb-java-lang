package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*

class ScopesResolver(val classesTable: Map<String, ClassOrInterface>) {
    data class Context(val symbolTable: SymbolTable, val returnType: Type, val classesTable: Map<String, ClassOrInterface>)

    fun check(method: Method, cls: Class): List<CompilerError> {
        val ctx = Context(SymbolTable(), method.returns, classesTable)
        ctx.symbolTable.enterScope()
        for (f in cls.fields)
            ctx.symbolTable.addSymbol(f)
        val result = mutableListOf<CompilerError>()
        for (statement in method.body)
            result.addAll(statement.check(ctx))
        return result
    }

}
