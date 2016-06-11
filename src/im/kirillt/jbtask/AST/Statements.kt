package im.kirillt.jbtask.AST

abstract class Statement

interface Expression

class MethodCall(val obj: Variable, val method: Method, val arguments: List<Expression>) : Statement(), Expression

class NewVar(val variable: Variable, val expr: Expression) : Statement(), Expression

class For(val loopVar: Variable, val collection: Variable) : Statement()

class IF(val condition: Expression, val thenBlock: List<Statement>, val elseBlock: List<Statement>) : Statement()

class Return(val expr:Expression): Statement()

//TODO: try/catch throw