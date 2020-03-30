package jude.plugins.literals

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins._
import nsc.transform._

class JudeLiterals(val global: Global) extends Plugin {
  import global._

  val name = "literals"
  val description = "converts scala literal values into jude literal values"
  val components = List[PluginComponent](Component)

  private object Component extends PluginComponent with TypingTransformers {
    val global: JudeLiterals.this.global.type = JudeLiterals.this.global

    val runsAfter = List[String]("parser")
    val phaseName = JudeLiterals.this.name
    def newPhase(_prev: Phase) = new JudeLiteralsPhase(_prev)

    class JudeLiteralsTransformer(unit: CompilationUnit)
        extends TypingTransformer(unit) {
      override def transform(tree: Tree) = tree match {
        case Literal(Constant(l: Long)) =>
          super.transform(q"""_root_.jude.i64($tree)""")
        case Literal(Constant(i: Int)) =>
          super.transform(q"""_root_.jude.i32($tree)""")
        case Literal(Constant(s: String)) =>
          super.transform(q"""_root_.jude.String($tree)""")
        case _ =>
          super.transform(tree)
      }
    }

    def newTransformer(unit: CompilationUnit) =
      new JudeLiteralsTransformer(unit)

    class JudeLiteralsPhase(prev: Phase) extends StdPhase(prev) {

      type PublicCompilationUnit = CompilationUnit
      override def name = JudeLiterals.this.name

      override def apply(unit: CompilationUnit): Unit =
        unit.body = new JudeLiteralsTransformer(unit).transform(unit.body)

    }
  }
}
