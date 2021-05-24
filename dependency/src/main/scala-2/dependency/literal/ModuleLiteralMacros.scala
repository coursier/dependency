package dependency
package literal

import dependency.parser.ModuleParser

import scala.reflect.macros.blackbox

// inspired from https://github.com/coursier/interface/blob/0bac6a4c93dfafeb79bd924a2f4bcb690ba4510a/interpolators/src/main/scala/coursierapi/Interpolators.scala

class ModuleLiteralMacros(override val c: blackbox.Context) extends LiteralMacros(c) {

  import c.universe._

  private def booleanOptExpr(value: Option[Boolean]): c.Expr[Option[Boolean]] = {
    val expr = value match {
      case None        => q"_root_.scala.None"
      case Some(false) => q"_root_.scala.Some(false)"
      case Some(true)  => q"_root_.scala.Some(true)"
    }
    c.Expr(expr)
  }

  protected def nameAttributesExpr(nameAttributes: NameAttributes): (Tree, c.Expr[NameAttributes]) = {
    val (tpe, expr) = nameAttributes match {
      case NoAttributes =>
        (tq"_root_.dependency.NoAttributes.type", q"_root_.dependency.NoAttributes")
      case ScalaNameAttributes(fullCrossVersion, platform) =>
        val tpe0 = tq"_root_.dependency.ScalaNameAttributes"
        val expr0 = q"_root_.dependency.ScalaNameAttributes(${booleanOptExpr(fullCrossVersion)}, ${booleanOptExpr(platform)})"
        (tpe0, expr0)
    }
    (tpe, c.Expr(expr))
  }

  private def stringStringMap(map: Map[String, String], mappings: Mappings): c.Expr[Map[String, String]] = {
    val entries = map.toVector.sorted.map { case (k, v) => c.Expr(q"_root_.scala.Tuple2(${applyMappings(k, mappings)}, ${applyMappings(v, mappings)})") }
    c.Expr(q"_root_.scala.collection.immutable.Map[_root_.java.lang.String, _root_.java.lang.String](..$entries)")
  }

  protected def moduleExpr(mod: AnyModule, mappings: Mappings): (Tree, c.Expr[AnyModule]) = {
    val (nameAttrTpe, nameAttr) = nameAttributesExpr(mod.nameAttributes)
    val attr = stringStringMap(mod.attributes, mappings)
    val expr = c.Expr(q"""
      _root_.dependency.ModuleLike[$nameAttrTpe](
        ${applyMappings(mod.organization, mappings)},
        ${applyMappings(mod.name, mappings)},
        $nameAttr,
        $attr
      )
    """)
    (nameAttrTpe, expr)
  }

  def module(args: c.Expr[Any]*): c.Expr[AnyModule] = {
    val inputs = unsafeGetPrefixStrings()
    val mappings0 = mappings(args)
    val input0 = input(inputs, mappings0)
    ModuleParser.parse(input0) match {
      case Left(msg) => c.abort(c.enclosingPosition, msg)
      case Right(mod) =>
        val (_, expr) = moduleExpr(mod, mappings0)
        expr
    }
  }
}
