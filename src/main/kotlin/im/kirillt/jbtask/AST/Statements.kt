package im.kirillt.jbtask.AST

import im.kirillt.jbtask.*
import im.kirillt.jbtask.builtin.BuiltInClasses.CheckedException
import im.kirillt.jbtask.builtin.BuiltInPrimitives.BoolType
import im.kirillt.jbtask.builtin.BuiltInPrimitives.VoidType
import im.kirillt.jbtask.builtin.BuiltInClasses.Iterable
import im.kirillt.jbtask.builtin.BuiltInClasses.Throwable

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
        val result = expr.check(ctx)
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
        val result = expr.check(ctx)
        val prev = ctx.symbolTable.findSymbol(variable.name)
        if (prev != null) {
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

class For(val loopVar: Variable, val collection: Type, val block: List<Statement>) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        ctx.symbolTable.enterScope()
        ctx.symbolTable.addSymbol(loopVar)
        if (collection !is Class)
            result.add(TypeCheckError(Iterable, collection, this))
        else {
            if (!collection.isChildOrSameAs(Iterable))
                result.add(TypeCheckError(Iterable, collection, this))
        }
        for (statement in block)
            result.addAll(statement.check(ctx))
        ctx.symbolTable.exitScope()
        return result
    }
}

class IF(val condition: Expression, val thenBlock: List<Statement>, val elseBlock: List<Statement>) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        result.addAll(condition.check(ctx))
        if (condition.type != BoolType())
            result.add(TypeCheckError(BoolType(), condition.type, this))
        ctx.symbolTable.enterScope()
        for (statement in elseBlock)
            result.addAll(statement.check(ctx))
        ctx.symbolTable.exitScope()
        ctx.symbolTable.enterScope()
        for (statement in elseBlock)
            result.addAll(statement.check(ctx))
        ctx.symbolTable.exitScope()
        return result
    }
}

//null means no value returned
class Return(val expr: Expression? = null) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        if (expr != null) {
            result.addAll(expr.check(ctx))
            if (expr.type != ctx.returnType)
                result.add(WrongReturnType(expr.type, this))
            return result
        } else {
            if (ctx.returnType != VoidType()) {
                result.add(WrongReturnType(VoidType(), this))
            }
        }
        return result
    }
}

class Throw(val expr: Expression) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        result.addAll(expr.check(ctx))
        var badType = false
        if (expr.type !is Class)
            badType = true
        else {
            if (expr.type is Class) {
                if (!expr.type.isChildOrSameAs(Throwable))
                    badType = true
                else {
                    if (expr.type.isChildOrSameAs(CheckedException) && !ctx.catchedExceptions.isCatched(expr.type))
                        result.add(ErrorInStatement("Unhandled exception '${expr.type}'", this))
                }
            }
        }
        if (badType)
            result.add(ErrorInStatement("Only subclasses of $Throwable can be thrown", this))
        return result
    }
}

class TryCatch(val exceptions: List<Class>, val tryBlock: List<Statement>, val catchBlock: List<Statement>) : Statement() {
    override fun check(ctx: ScopesResolver.Context): MutableList<CompilerError> {
        val result = mutableListOf<CompilerError>()
        val correct = mutableListOf<Class>()
        for (e in exceptions) {
            if (!e.isChildOrSameAs(Throwable))
                result.add(ErrorInStatement("Only subclasses of $Throwable can be caught", this))
            else
                correct += e
        }
        //TODO: remove copy-paste
        ctx.catchedExceptions.enterScope()
        correct.forEach { ctx.catchedExceptions.addException(it) }
        ctx.symbolTable.enterScope()
        for (statement in tryBlock)
            result.addAll(statement.check(ctx))
        ctx.symbolTable.exitScope()
        ctx.catchedExceptions.exitScope()

        ctx.symbolTable.enterScope()
        for (statement in catchBlock)
            result.addAll(statement.check(ctx))
        ctx.symbolTable.exitScope()
        return result
    }
}
