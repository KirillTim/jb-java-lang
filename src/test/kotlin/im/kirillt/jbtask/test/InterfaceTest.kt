package im.kirillt.jbtask.test

import im.kirillt.jbtask.AST.Interface
import im.kirillt.jbtask.AST.Method
import im.kirillt.jbtask.AST.Modifiers
import im.kirillt.jbtask.AST.Variable
import im.kirillt.jbtask.DeclarationError
import im.kirillt.jbtask.builtin.BuiltInPrimitives.VoidType
import im.kirillt.jbtask.builtin.BuiltInPrimitives.IntegerType
import org.junit.rules.ExpectedException
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestName

import org.junit.Assert.assertThat;
import org.hamcrest.CoreMatchers.containsString;

//TODO: more tests
class InterfaceTest {

    val name = TestName()
    val expected = ExpectedException.none()

    //just workaround from google, don't know how it works exactly
    @Rule
    fun rules() = RuleChain.outerRule(name).around(expected)

    @Test
    fun testOK() {
        val method1 = Method("fun", VoidType(), Modifiers())
        val method2 = Method("fun", VoidType(), Modifiers(), listOf(Variable("i", IntegerType())))
        val interf = Interface("interf", listOf(method1, method2))
        //i know there are tons of asserts for collections
        assertTrue(interf.getPublicMethods().contains(method1))
        assertTrue(interf.getPublicMethods().contains(method2))
    }

    @Test
    fun testIncompatibleReturnType() {
        var msg = ""
        try {
            val method1 = Method("fun", VoidType(), Modifiers())
            val method2 = Method("fun", IntegerType(), Modifiers())
            Interface("interf", listOf(method1, method2))
        } catch (e: DeclarationError) {
            msg = e.message!!
        }
        //it's ugly, i know
        assertThat(msg, containsString("fun"));
        assertThat(msg, containsString("incompatible return type"));
    }

    @Test
    fun testReDeclaration() {
        var msg = ""
        try {
            val method1 = Method("fun", VoidType(), Modifiers())
            val method2 = Method("fun", VoidType(), Modifiers())
            Interface("interf", listOf(method1, method2))
        } catch (e: DeclarationError) {
            msg = e.message!!
        }
        assertThat(msg, containsString("fun"));
        assertThat(msg, containsString("is already defined"));
    }

    @Test
    fun testExtends() {
        val method1 = Method("fun", VoidType(), Modifiers())
        val method2 = Method("funInt", IntegerType(), Modifiers())
        val i1 = Interface("i1", listOf(method1))
        val i2 = Interface("i2", listOf(method2), listOf(i1))
        assertTrue(i2.getPublicMethods().contains(method1))
        assertTrue(i2.getPublicMethods().contains(method2))

    }
}
