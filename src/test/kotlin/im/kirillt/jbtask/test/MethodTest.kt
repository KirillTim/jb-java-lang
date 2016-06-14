package im.kirillt.jbtask.test

import im.kirillt.jbtask.AST.Class
import im.kirillt.jbtask.AST.Method
import im.kirillt.jbtask.AST.Modifiers
import im.kirillt.jbtask.ASTException
import im.kirillt.jbtask.builtin.BuiltInClasses.CheckedException
import im.kirillt.jbtask.builtin.BuiltInPrimitives.IntegerType
import org.junit.Test

import org.junit.Assert.assertThat;
import org.hamcrest.CoreMatchers.containsString;


class MethodTest {
    @Test
    fun throwsTypesTestError() {
        val cls = Class("Bad", Modifiers(), listOf(), listOf())
        var msg = ""
        try {
            val method = Method("method", IntegerType(), Modifiers(), throws = listOf(cls))
        } catch (e : ASTException) {
            msg = e.msg
        }
        assertThat(msg, containsString("Only subclasses of CheckedException can be thrown"));
    }

    @Test
    fun throwsTypesTestOK() {
        val cls = Class("Good", Modifiers(), listOf(), listOf(), extends = CheckedException)
        val method = Method("method", IntegerType(), Modifiers(), throws = listOf(cls))
    }
}
