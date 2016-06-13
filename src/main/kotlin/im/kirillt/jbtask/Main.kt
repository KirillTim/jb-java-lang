package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*

class Void() : Type("void")

fun main(args:Array<String>) {
    val m1 = Method("m1", Void(), Modifiers(Visibility.PRIVATE), hasBody = true)
    val obj = Class("obj", Modifiers(), listOf(), listOf(m1))
    println(obj.name)
}