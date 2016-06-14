package im.kirillt.jbtask.builtin

import im.kirillt.jbtask.AST.*

object BuiltInPrimitives {
    class VoidType() : Type("void")

    class BoolType() : Type("bool")

    class IntegerType() : Type("int")

    class StringType() : Type("string")
}

object BuiltInClasses {
    //base class for loops
    val Iterable = Class("Iterable", Modifiers(), listOf(), listOf())
    //base class for all exceptions
    val Throwable = Class("Throwable", Modifiers(isAbstract = true), listOf(), listOf())
    //base class for checked exceptions
    val CheckedException = Class("CheckedException", Modifiers(), listOf(), listOf(), Throwable)
}

object Literals {
    val stringLiteral = Literal(BuiltInPrimitives.StringType())
    val intLiteral = Literal(BuiltInPrimitives.IntegerType())
}
