package im.kirillt.jbtask.AST

abstract class Type(val name: String)


class Interface(name: String,
                val methods: List<Method>,
                val extends: List<Interface> = listOf()) : Type(name)

class Class(name: String,
            val modifiers: Modifiers,
            val fields: List<Field>,
            val methods: List<Method>,
            val extends: Class? = null,
            val implements: List<Interface> = listOf())

enum class Visibility {
    PRIVATE, PUBLIC, PROTECTED
}

class Modifiers(val visibility: Visibility = Visibility.PUBLIC,
                val abstract: Boolean = false,
                val final: Boolean = false,
                val static: Boolean = false)

class Method(val name: String,
             val returns: Type,
             val modifiers: Modifiers,
             val parameters: List<Variable> = listOf(),
             val throws: List<Type> = listOf())

class Field(val name: String, val type: Type, modifiers: Modifiers)

data class Variable(val name:String, val type: Type)