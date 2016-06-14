package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*

open class CompilerError(val msg: String) {
    override fun toString(): String {
        return "Compiler error : '$msg'"
    }
}

open class ErrorInStatement(msg: String, method: Method, statement: Statement)
: CompilerError("$msg in statement '$statement' in method '$method'")

open class UnknownVariable(variable: Variable, method: Method, statement: Statement)
: ErrorInStatement("Unknown variable '${variable.name}'", method, statement)

open class UnknownType(type: Type, method: Method, statement: Statement)
: ErrorInStatement("Unknown type '${type.name}'", method, statement)

open class WrongReturnType(type: Type, method: Method, statement: Statement)
: ErrorInStatement("Wrong return type '${type.name}'", method, statement)

open class NoSuchMethod(where: ClassOrInterface, method: Method)
: CompilerError("${where.name} have no method $method")

open class NoSuchField(where: ClassOrInterface, field: Field)
: CompilerError("${where.name} have no field ${field.name}")

open class ReAssignToFinal(variable: Variable, method: Method, statement: Statement)
: ErrorInStatement("${variable.name} cannot be re-assigned", method, statement)
