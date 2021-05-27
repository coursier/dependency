package dependency
package literal

import scala.reflect.macros.blackbox
import dependency.parser.DependencyParser

class DependencyLiteralMacros(override val c: blackbox.Context) extends ModuleLiteralMacros(c) {
  import c.universe._

  private def optionString(opt: Option[String], mappings: Mappings): c.Expr[Option[String]] = {
    val expr = opt match {
      case None => q"_root_.scala.None"
      case Some(value) => q"_root_.scala.Some(${applyMappings(value, mappings)})"
    }
    c.Expr(expr)
  }

  private def stringOptionStringMap(map: Map[String, Option[String]], mappings: Mappings): c.Expr[Map[String, Option[String]]] = {
    val entries = map.toVector.sorted.map {
      case (k, v) =>
        val value = optionString(v, mappings)
        c.Expr(q"_root_.scala.Tuple2(${applyMappings(k, mappings)}, $value)")
    }
    c.Expr(q"_root_.scala.collection.immutable.Map[_root_.java.lang.String, _root_.scala.Option[_root_.java.lang.String]](..$entries)")
  }

  private def dependencyExpr(dep: AnyDependency, mappings: Mappings): c.Expr[AnyDependency] = {
    val (nameAttr, modExpr) = moduleExpr(dep.module, mappings)
    val params = stringOptionStringMap(dep.userParams, mappings)
    val exclude = dep.exclude.map(moduleExpr(_, mappings)._2)
    c.Expr(q"""
      _root_.dependency.DependencyLike[$nameAttr, _root_.dependency.NameAttributes](
        $modExpr,
        ${applyMappings(dep.version, mappings)},
        _root_.dependency.CovariantSet[_root_.dependency.AnyModule](..$exclude),
        $params
      )
    """)
  }

  def dependency(args: c.Expr[Any]*): c.Expr[AnyDependency] = {
    val inputs = unsafeGetPrefixStrings()
    val mappings0 = mappings(args)
    val input0 = input(inputs, mappings0)
    DependencyParser.parse(input0) match {
      case Left(msg) => c.abort(c.enclosingPosition, msg)
      case Right(dep) => dependencyExpr(dep, mappings0)
    }
  }
}
