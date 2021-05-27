package dependency
package literal

import dependency.parser.{DependencyParser, ModuleParser}

import scala.quoted._

// some inspiration from https://github.com/typelevel/literally/blob/d1f4e84e1b521cfe25805aacc3b9e225fd7cbc26/core/shared/src/main/scala-3/org/typelevel/literally/Literally.scala

object Extensions {

  private def inputStrings(strCtxExpr: Expr[StringContext])(using Quotes): Seq[String] =
    strCtxExpr.value match {
      case None =>
        quotes.reflect.report.error("StringContext args must be statically known")
        ???
      case Some(sc) =>
        sc.parts
    }

  private def option[T: ToExpr : Type](opt: Option[T])(using Quotes): Expr[Option[T]] =
    opt match {
      case None => '{None}
      case Some(value) => '{Some(${Expr(value)})}
    }

  private def nameAttr(nameAttr: NameAttributes)(using Quotes): Expr[NameAttributes] =
    nameAttr match {
      case NoAttributes => '{NoAttributes}
      case ScalaNameAttributes(fullCrossVersion, platform) =>
        '{ScalaNameAttributes(${option(fullCrossVersion)}, ${option(platform)})}
    }

  private def module(mod: ModuleLike[NameAttributes], mappings: Mappings)(using Quotes): Expr[ModuleLike[NameAttributes]] =
    '{
      ModuleLike(
        ${mappings.Expr(mod.organization)},
        ${mappings.Expr(mod.name)},
        ${nameAttr(mod.nameAttributes)},
        ${mappings.mapStringString(mod.attributes)}
      )
    }

  private def dependency(dep: DependencyLike[NameAttributes, NameAttributes], mappings: Mappings)(using Quotes): Expr[DependencyLike[NameAttributes, NameAttributes]] = {
    val excludes = dep.exclude.toVector.sortBy(_.toString).map(module(_, mappings))
    '{
      DependencyLike(
        ${module(dep.module, mappings)},
        ${mappings.Expr(dep.version)},
        CovariantSet(${Varargs(excludes)}: _*),
        ${mappings.mapStringStringOption(dep.userParams)}
      )
    }
  }

  def parseModule(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[ModuleLike[NameAttributes]] = {

    val inputs = inputStrings(strCtxExpr)
    val mappings = Mappings.from(inputs, argsExpr)
    val input = mappings.input(inputs)

    ModuleParser.parse(input) match {
      case Left(msg) =>
        quotes.reflect.report.error(s"parsing module failed: $msg")
        ???
      case Right(mod) =>
        module(mod, mappings)
    }
  }

  def parseDependency(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using Quotes): Expr[DependencyLike[NameAttributes, NameAttributes]] = {

    val inputs = inputStrings(strCtxExpr)
    val mappings = Mappings.from(inputs, argsExpr)
    val input = mappings.input(inputs)

    DependencyParser.parse(input) match {
      case Left(msg) =>
        quotes.reflect.report.error(s"parsing dependency failed: $msg", argsExpr)
        ???
      case Right(dep) =>
        dependency(dep, mappings)
    }
  }
}

trait Extensions {
  extension (inline sc: StringContext)
    inline def mod(inline args: Any*): ModuleLike[NameAttributes] =
      ${Extensions.parseModule('sc, 'args)}
    inline def dep(inline args: Any*): DependencyLike[NameAttributes, NameAttributes] =
      ${Extensions.parseDependency('sc, 'args)}
}
