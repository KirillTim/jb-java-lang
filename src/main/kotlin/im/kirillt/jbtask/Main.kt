package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*
import im.kirillt.jbtask.builtin.IntegerType
import im.kirillt.jbtask.builtin.VoidType

fun main(args:Array<String>) {
    val f1 = Field("f1", IntegerType(), Modifiers())
    val m1 = Method("f1m", IntegerType(), Modifiers(), body = listOf(EmptyStatement()))
    val F = Class("F", Modifiers(), listOf(f1), listOf(m1))
    val m2Int = Method("g1mInt", F, Modifiers(), listOf(Variable("param1", IntegerType())), body = listOf(EmptyStatement()))
    val varF = Variable("varF", F)
    val statements = listOf<Statement>(
            VarCreation(varF, New(F)),
            VarCreation(Variable("varInt", IntegerType()), FieldRef(VarRef(varF).type, f1)),
            MethodCall(VarRef(varF).type,  m2Int, listOf()),
            Return(VarRef(Variable("varInt", IntegerType())))
    )
    val m2 = Method("g1m", F, Modifiers(),body = statements)

    val G = Class("G", Modifiers(), listOf(), listOf(m2, m2Int))
    val classesTable = mutableMapOf<String, ClassOrInterface>()
    classesTable["F"] = F
    classesTable["G"] = G
    val resolver = ScopesResolver(classesTable)
    val res = resolver.check(m2, G)
    res.forEach { println(it) }
}