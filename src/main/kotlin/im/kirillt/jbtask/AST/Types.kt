package im.kirillt.jbtask.AST

import im.kirillt.jbtask.DeclarationError

abstract class Type(val name: String) {
    override fun equals(other: Any?) = other is Type && other.name == name

    override fun hashCode() = name.hashCode()
}

abstract class ClassOrInterface(name: String) : Type(name) {
    fun checkReturnTypeOverload(method: Method, all: List<Method>): Boolean {
        for (i in all) {
            if (method === i)
                continue
            if (method.name == i.name && method.argumentsTypes == i.argumentsTypes && method.returns != i.returns)
                throw DeclarationError(this, method, "incompatible return type")
        }
        return true
    }

    fun checkReDeclaration(method: Method, all: List<Method>): Boolean {
        for (i in all) {
            if (method === i)
                continue
            if (method.nameAndSignature == i.nameAndSignature)
                throw DeclarationError(this, method, "is already defined")
        }
        return true
    }
}

class Null() : Type("null")

class Interface(name: String,
                val methods: List<Method>,
                val extends: List<Interface> = listOf()) : ClassOrInterface(name) {
    init {
        val all = getAllMethods()
        for (m in methods) {
            checkMethodModifiers(m)
            checkReturnTypeOverload(m, all)
            checkReDeclaration(m, methods)
            if (m.hasBody)
                throw DeclarationError(this, m, "cannot have body")
        }
    }

    //TODO: return list of errors
    private fun checkMethodModifiers(method: Method): Boolean {
        val visibility = method.modifiers.visibility
        if (visibility == Visibility.PRIVATE || visibility == Visibility.PROTECTED)
            throw DeclarationError(this, method, "cannot be $visibility")
        if (method.modifiers.isFinal)
            throw DeclarationError(this, method, "cannot be final")
        return true
    }

    fun getAllMethods(): List<Method> =
            methods.toMutableList() + extends.flatMap { it.getAllMethods() }.distinctBy { it.nameAndSignature }

}

class Class(name: String,
            val modifiers: Modifiers,
            val fields: List<Field>,
            val methods: List<Method>,
            val extends: Class? = null,
            val implements: List<Interface> = listOf()) : ClassOrInterface(name) {

    init {
        val shouldBeImplemented = implements.flatMap { it.getAllMethods() }.toMutableList()
        if (extends != null) {
            if (extends.modifiers.isFinal)
                throw DeclarationError(this, "cannot inherit from final ${extends.name}")
            shouldBeImplemented.addAll(extends.methods.filter { it.modifiers.isAbstract })
        }
        for (m in methods) {
            checkMethodModifiers(m)
            checkReDeclaration(m, methods)
            if (extends != null) {
                for (i in extends.getAllMethods()) {
                    if (!i.modifiers.isAbstract)
                        shouldBeImplemented.removeAll { it.nameAndSignature == i.nameAndSignature }
                    if (i.modifiers.isFinal && m.nameAndSignature == i.nameAndSignature)
                        throw DeclarationError(this, m, "cannot override final method")
                }
            }
            shouldBeImplemented.removeAll { it.nameAndSignature == m.nameAndSignature }
        }
        if (shouldBeImplemented.isNotEmpty() && !modifiers.isAbstract)
            throw DeclarationError(this, shouldBeImplemented.map { it.name }.fold("Methods: ") { acc, s -> acc + s + ", " } + "should be implemented")

    }

    //TODO: return list of errors
    private fun checkMethodModifiers(method: Method): Boolean {
        if (method.modifiers.isAbstract) {
            if (!this.modifiers.isAbstract)
                throw DeclarationError(this, method, "abstract method in non-abstract class")
            if (method.hasBody)
                throw DeclarationError(this, method, "abstract method cannot have a body")
            if (method.modifiers.visibility == Visibility.PRIVATE)
                throw DeclarationError(this, method, "abstract method cannot be private")
            if (method.modifiers.isFinal)
                throw DeclarationError(this, method, "abstract method cannot be final")
        } else {
            if (!method.hasBody)
                throw DeclarationError(this, method, "method should be abstract or have a body")
        }
        return true
    }

    //TODO: return 'last overrided' version of each method
    fun getAllMethods(): List<Method> {
        val result = implements.flatMap { it.getAllMethods() }.toMutableList()
        if (extends != null)
            result.addAll(extends.getAllMethods())
        result.addAll(methods)
        return result.distinctBy { it.nameAndSignature }
    }
}

enum class Visibility {
    PRIVATE {
        override fun toString() = "private"
    },
    PROTECTED {
        override fun toString() = "protected"
    },
    PUBLIC {
        override fun toString() = "public"
    }
}

class Modifiers(val visibility: Visibility = Visibility.PUBLIC,
                val isAbstract: Boolean = false,
                val isFinal: Boolean = false,
                val isStatic: Boolean = false)

class Method(val name: String,
             val returns: Type,
             val modifiers: Modifiers,
             val parameters: List<Variable> = listOf(),
             val throws: List<Type> = listOf(),
             val hasBody: Boolean = true) {
    val argumentsTypes: List<Type> = parameters.map { it.type }

    data class NameAndSignature(val name: String, val returns: Type, val argumentsTypes: List<Type>)

    val nameAndSignature = NameAndSignature(name, returns, argumentsTypes)
}

class Field(val name: String, val type: Type, modifiers: Modifiers)

data class Variable(val name: String, val type: Type)