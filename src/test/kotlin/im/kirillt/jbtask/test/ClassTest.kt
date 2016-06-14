package im.kirillt.jbtask.test

import im.kirillt.jbtask.AST.*
import im.kirillt.jbtask.DeclarationError
import im.kirillt.jbtask.builtin.VoidType
import im.kirillt.jbtask.builtin.IntegerType
import org.junit.rules.ExpectedException
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestName

import org.junit.Assert.assertThat;
import org.hamcrest.CoreMatchers.containsString;
import org.junit.Assert

class ClassTest {

    val name = TestName()
    val expected = ExpectedException.none()

    //just workaround from google, don't know how it works exactly
    @Rule
    fun rules() = RuleChain.outerRule(name).around(expected)

    @Test
    fun testOk() {
        val method1 = Method("fun", VoidType(), Modifiers())
        val method2 = Method("fun", VoidType(), Modifiers(), listOf(Variable("i", IntegerType())))
        val interf = Interface("interf", listOf(method1, method2))
        val method1Impl1 = Method("fun", VoidType(), Modifiers(), body = listOf(EmptyStatement()))
        val method1Impl2 = Method("fun", VoidType(), Modifiers(), listOf(Variable("i", IntegerType())), body = listOf(EmptyStatement()))
        val class1 = Class("class1", Modifiers(isAbstract = true), listOf(), listOf(method1Impl1), implements = listOf(interf))
        val class2 = Class("class2", Modifiers(), listOf(), listOf(method1Impl2), class1)
        //this test will fail if i fix `getAllMethods()`
        assertTrue(class2.getPublicMethods().contains(method1))
        assertTrue(class2.getPublicMethods().contains(method2))
        assertTrue(class2.getPublicMethods().size == 2)
        assertTrue(class2.extends == class1)
    }

    @Test
    fun testAbstract() {

    }

    @Test
    fun testFinal() {

    }

    @Test
    fun implementMe() {

    }
}
