package im.kirillt.jbtask

import im.kirillt.jbtask.AST.*
import im.kirillt.jbtask.builtin.VoidType

fun main(args:Array<String>) {
    //val m1 = Method("m1", VoidType(), Modifiers(Visibility.PRIVATE))
    //val obj = Class("obj", Modifiers(), listOf(), listOf(m1))
    //println(obj.name)
    val cls = Class("class", Modifiers(),listOf(), listOf())
    val interf = Interface("class", listOf())
    println(cls.equals(interf))
}