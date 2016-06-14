package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*

open class CompilerError(val msg: String) {
    override fun toString(): String {
        return "Compiler error : '$msg'"
    }
}

class ErrorsInMethod(val method: Method, val errors: List<CompilerError>)

open class ErrorInStatement(msg: String, statement: Statement)
: CompilerError("$msg in statement '$statement'")

open class UnknownVariable(variable: Variable, statement: Statement)
: ErrorInStatement("Unknown variable '${variable}'", statement)

open class UnknownType(type: Type, statement: Statement)
: ErrorInStatement("Unknown type '${type.name}'", statement)

open class TypeCheckError(expected: Type, real:Type, statement: Statement)
: ErrorInStatement("Type check error: '${expected.name}' expected, but '${real.name}' found", statement)

open class WrongReturnType(type: Type, statement: Statement)
: ErrorInStatement("Wrong return type '${type.name}'", statement)

open class ReAssignToFinal(variable: Variable, statement: Statement)
: ErrorInStatement("variable '${variable}' cannot be re-assigned", statement)

open class VariableAlreadyDefined(variable: Variable, statement: Statement)
: ErrorInStatement("variable '${variable}' has already defined in this scope", statement)

open class NoSuchMethod(where: ClassOrInterface, method: Method)
: CompilerError("${where.name} have no method $method")

open class NoSuchField(where: ClassOrInterface, field: Variable)
: CompilerError("${where.name} have no field ${field.name}")