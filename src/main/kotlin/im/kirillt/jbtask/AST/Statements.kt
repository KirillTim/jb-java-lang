package im.kirillt.jbtask.AST

import im.kirillt.jbtask.*

abstract class Statement {
    abstract fun check(ctx: ScopesResolver.Context): MutableList<CompilerError>
}

class EmptyStatement() : Statement() {
    override fun check(ctx: ScopesResolver.Context) = mutableListOf<CompilerError>()
}

abstract class Expression(val type: Type) : Statement()

class Literal(type: Type) : Expression(type) {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> = mutableListOf()
}

class VarRef(val variable: Variable) : Expression(variable.type) {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        if (ctx.symbolTable.findSymbol(variable.name) == null)
            result += UnknownVariable(variable, this)
        return result
    }
}

class FieldRef(val from: Type, val field: Variable) : Expression(field.type) {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        if (from !is Class) {
            result.add(ErrorInStatement("can't access field for primitive type", this)) //TODO: fix
            return result
        }
        val cls = ctx.classesTable[from.name]
        if (cls != null) {
            if (!cls.getPublicFields().any { it.name == field.name })
                result.add(NoSuchField(cls, field))
        } else {
            result.add(UnknownType(from, this))
        }
        return result
    }
}

class MethodCall(val from: Type, val method: Method, val arguments: List<Expression>) : Expression(method.returns) {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        result.addAll(arguments.flatMap { it.check(ctx) })
        if (from !is ClassOrInterface) {
            result.add(ErrorInStatement("can't access field for primitive type", this)) //TODO: fix
            return result
        }
        val cls = ctx.classesTable[from.name]
        if (cls != null) {
            if (!cls.getPublicMethods().any { it.nameAndSignature == method.nameAndSignature })
                result.add(NoSuchMethod(cls, method))
            val argTypes = arguments.map { it.type }
            if (method.argumentsTypes != argTypes)
                result.add(ErrorInStatement("Incompatible types", this))
        } else {
            result.add(UnknownType(from, this))
        }
        return result
    }
}

class New(cls: Class) : Expression(cls) {
    //TODO: add constructors support
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        return mutableListOf<CompilerError>()
    }
}

class VarCreation(val variable: Variable, val expr: Expression) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result =  expr.check(ctx)
        if (ctx.symbolTable.isDefinedInCurrentScope(variable.name))
            result.add(VariableAlreadyDefined(variable, this))
        else {
            ctx.symbolTable.addSymbol(variable)
            if (expr.type != variable.type)
                result.add(TypeCheckError(variable.type, expr.type, this))
        }
        return result
    }
}

class VarAssignment(val variable: Variable, val expr: Expression) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result =  expr.check(ctx)
        val prev = ctx.symbolTable.findSymbol(variable.name)
        if  (prev != null) {
            if (prev.isFinal)
                result.add(ReAssignToFinal(variable, this))
            else {
                if (prev.type != variable.type)
                    result.add(TypeCheckError(prev.type, variable.type, this))
            }
        } else {
            result += UnknownVariable(variable, this)
        }
        return result
    }
}

class For(val loopVar: Variable, val collection: Variable, val block: List<Statement>) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        throw UnsupportedOperationException()
    }
}

class IF(val condition: Expression, val thenBlock: List<Statement>, val elseBlock: List<Statement>) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        throw UnsupportedOperationException()
    }
}

class Return(val expr: Expression) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = expr.check(ctx)
        if (expr.type != ctx.returnType)
            result.add(WrongReturnType(expr.type, this))
        return result
    }
}

//TODO: try/catch throw