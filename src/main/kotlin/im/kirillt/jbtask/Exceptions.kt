package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*

open class ASTException(msg: String) : Throwable(msg)

open class DeclarationError : ASTException {
    constructor(what: Type, msg: String) : super("${what.name}: $msg")

    constructor(where: ClassOrInterface, what: Method, msg: String)
    : super("${getType(where)} ${where.name}, method ${what.name}: $msg")

    constructor(where: Class, what: Field, msg: String) : super("Class ${where.name}, field ${what.name}: $msg")

    companion object {
        private fun getType(what: ClassOrInterface) = if (what is Class) "Class" else "Interface"
    }
}