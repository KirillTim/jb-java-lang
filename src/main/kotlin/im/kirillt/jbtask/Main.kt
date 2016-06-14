package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*
import im.kirillt.jbtask.builtin.BuiltInClasses.CheckedException
import im.kirillt.jbtask.builtin.BuiltInClasses.Throwable
import im.kirillt.jbtask.builtin.BuiltInPrimitives.IntegerType
import im.kirillt.jbtask.builtin.BuiltInPrimitives.StringType
import im.kirillt.jbtask.builtin.BuiltInPrimitives.VoidType
import im.kirillt.jbtask.builtin.BuiltInPrimitives.BoolType
import im.kirillt.jbtask.builtin.Literals.IntLiteral
import im.kirillt.jbtask.builtin.Literals.BoolLiteral
import im.kirillt.jbtask.builtin.Literals.StringLiteral

fun main(args: Array<String>) {
    test1()
    test2()
    test3()
}

fun test3() {
    val RunTimeEx = Class("RunTimeEx", Modifiers(), listOf(), listOf(), Throwable)
    val RunTimeExExtended = Class("RunTimeExExtended", Modifiers(), listOf(), listOf(), RunTimeEx)
    val CheckedEx = Class("CheckedEx", Modifiers(), listOf(), listOf(), CheckedException)
    val CheckedExExtended = Class("CheckedExExtended", Modifiers(), listOf(), listOf(), CheckedEx)
    val statements = listOf<Statement>(
            Throw(New(RunTimeEx)), //OK
            Throw(New(CheckedEx)), //Error
            TryCatch(listOf(CheckedEx, RunTimeEx),
                    listOf(Throw(New(CheckedExExtended)), Throw(New(CheckedEx)))/*OK*/,
                    listOf(EmptyStatement())),
            Return()
    )
    val m1 = Method("f1m", VoidType(), Modifiers(), body = statements, throws = listOf(CheckedExExtended))
    val F = Class("F", Modifiers(), listOf(), listOf(m1))
    val classesTable = mutableMapOf<String, ClassOrInterface>()
    classesTable["F"] = F
    val resolver = ScopesResolver(classesTable)
    val res = resolver.check(m1, F)
    res.forEach { println(it) }
    /*
    * Compiler error : 'Unhandled exception 'CheckedEx' in statement 'im.kirillt.jbtask.AST.Throw@7699a589''
    * */
}

fun test2() {
    //loop var type error
    //int literal
    //wrong return type
    //for loop scope
    val f1 = Field("f1", IntegerType(), Modifiers())
    val badLoopVar = Variable("varQ", IntegerType())
    val XInt = Variable("X", IntegerType())
    val XStr = Variable("X", StringType())
    val XBool = Variable("X", BoolType())
    val statements = listOf<Statement>(
            VarInitialization(XInt, IntLiteral),
            VarInitialization(XStr, StringLiteral),
            VarInitialization(badLoopVar, IntLiteral),
            For(f1, VarRef(badLoopVar), listOf(VarInitialization(XBool, BoolLiteral))),
            Return(VarRef(f1))
    )
    val m1 = Method("f1m", VoidType(), Modifiers(), body = statements)
    val F = Class("F", Modifiers(), listOf(f1), listOf(m1))
    val classesTable = mutableMapOf<String, ClassOrInterface>()
    classesTable["F"] = F
    val resolver = ScopesResolver(classesTable)
    val res = resolver.check(m1, F)
    res.forEach { println(it) }
    /*
    * Compiler error : 'variable 'X: string' has already defined in this scope in statement 'im.kirillt.jbtask.AST.VarCreation@7699a589''
    * Compiler error : 'Type check error: 'Iterable' expected, but 'int' found in statement 'im.kirillt.jbtask.AST.For@58372a00''
    * Compiler error : 'Wrong return type 'int' in statement 'im.kirillt.jbtask.AST.Return@16b98e56''
    * */
}

fun test1() {
    val f1 = Field("f1", IntegerType(), Modifiers())
    val m1 = Method("f1m", IntegerType(), Modifiers(), body = listOf(EmptyStatement()))
    val F = Class("F", Modifiers(), listOf(f1), listOf(m1))
    val m2Int = Method("g1mInt", F, Modifiers(), listOf(Variable("param1", IntegerType())), body = listOf(EmptyStatement()))
    val varF = Variable("varF", F)
    val statements = listOf<Statement>(
            VarInitialization(varF, New(F)),
            VarInitialization(Variable("varInt", IntegerType()), FieldRef(VarRef(varF), f1)),
            MethodCall(VarRef(varF), m2Int, listOf()),
            Return(VarRef(Variable("varInt", IntegerType())))
    )
    val m2 = Method("g1m", F, Modifiers(), body = statements)

    val G = Class("G", Modifiers(), listOf<Field>(Field("VarF", StringType(), Modifiers())), listOf(m2, m2Int))
    //VarF in class G shows that scoping works
    val classesTable = mutableMapOf<String, ClassOrInterface>()
    classesTable["F"] = F
    classesTable["G"] = G
    val resolver = ScopesResolver(classesTable)
    val res = resolver.check(m2, G)
    res.forEach { println(it) }
    /*
    * Compiler error : 'F have no method  public im.kirillt.jbtask.AST.Class@46 g1mInt(im.kirillt.jbtask.builtin.BuiltInPrimitives$IntegerType@197ef) '
    * Compiler error : 'Incompatible types in statement 'im.kirillt.jbtask.AST.MethodCall@7699a589''
    * Compiler error : 'Wrong return type 'int' in statement 'im.kirillt.jbtask.AST.Return@58372a00''
    * */
}