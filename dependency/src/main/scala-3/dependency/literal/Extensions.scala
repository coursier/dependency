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

  private def simpleNameAttr(nameAttr: NameAttributes)(using Quotes): Expr[NameAttributes] =
    nameAttr match {
      case NoAttributes => '{NoAttributes}
      case ScalaNameAttributes(fullCrossVersion, platform) =>
        '{ScalaNameAttributes(${option(fullCrossVersion)}, ${option(platform)})}
    }

  private def noAttrModule(mod: ModuleLike[NameAttributes], mappings: Mappings)(using Quotes): Expr[ModuleLike[NoAttributes.type]] =
    '{
      ModuleLike[NoAttributes.type](
        ${mappings.Expr(mod.organization)},
        ${mappings.Expr(mod.name)},
        NoAttributes,
        ${mappings.mapStringString(mod.attributes)}
      )
    }

  private def scalaAttrModule(mod: ModuleLike[NameAttributes], mappings: Mappings)(using Quotes): Expr[ModuleLike[ScalaNameAttributes]] =
    '{
      ModuleLike[ScalaNameAttributes](
        ${mappings.Expr(mod.organization)},
        ${mappings.Expr(mod.name)},
        ScalaNameAttributes(${option(mod.nameAttributes.asInstanceOf[ScalaNameAttributes].fullCrossVersion)}, ${option(mod.nameAttributes.asInstanceOf[ScalaNameAttributes].platform)}),
        ${mappings.mapStringString(mod.attributes)}
      )
    }

  private def module(mod: ModuleLike[NameAttributes], mappings: Mappings)(using Quotes): Expr[ModuleLike[NameAttributes]] =
    if (mod.nameAttributes == NoAttributes)
      '{
        ModuleLike[NoAttributes.type](
          ${mappings.Expr(mod.organization)},
          ${mappings.Expr(mod.name)},
          NoAttributes,
          ${mappings.mapStringString(mod.attributes)}
        )
      }
    else
      '{
        ModuleLike[ScalaNameAttributes](
          ${mappings.Expr(mod.organization)},
          ${mappings.Expr(mod.name)},
          ScalaNameAttributes(${option(mod.nameAttributes.asInstanceOf[ScalaNameAttributes].fullCrossVersion)}, ${option(mod.nameAttributes.asInstanceOf[ScalaNameAttributes].platform)}),
          ${mappings.mapStringString(mod.attributes)}
        )
      }

  private def simpleModule(mod: ModuleLike[NameAttributes], mappings: Mappings)(using Quotes): Expr[ModuleLike[NameAttributes]] =
    '{
      ModuleLike[NameAttributes](
        ${mappings.Expr(mod.organization)},
        ${mappings.Expr(mod.name)},
        ${simpleNameAttr(mod.nameAttributes)},
        ${mappings.mapStringString(mod.attributes)}
      )
    }

  private def dependency(dep: DependencyLike[NameAttributes, NameAttributes], mappings: Mappings)(using Quotes): Expr[DependencyLike[NameAttributes, NameAttributes]] = {
    val hasScalaMod = dep.module.nameAttributes != NoAttributes
    val allJavaExcludes = dep.exclude.forall(_.nameAttributes == NoAttributes)
    val allScalaExcludes = dep.exclude.forall(_.nameAttributes.isInstanceOf[ScalaNameAttributes])
    val hasExcludes = dep.exclude.nonEmpty

    // can't find a reasonable way to abstract over this with the quoted API…
    (hasScalaMod, hasExcludes, allJavaExcludes, allScalaExcludes) match {
      case (false, false, _, _) | (false, true, true, _) =>
        val module0 = noAttrModule(dep.module, mappings)
        val excludes = dep.exclude.toVector.sortBy(_.toString).map(noAttrModule(_, mappings))
        '{
          DependencyLike[NoAttributes.type, NoAttributes.type](
            $module0,
            ${mappings.Expr(dep.version)},
            CovariantSet(${Varargs(excludes)}: _*),
            ${mappings.mapStringStringOption(dep.userParams)}
          )
        }
      case (false, true, false, true) =>
        val module0 = noAttrModule(dep.module, mappings)
        val excludes = dep.exclude.toVector.sortBy(_.toString).map(scalaAttrModule(_, mappings))
        '{
          DependencyLike[NoAttributes.type, ScalaNameAttributes](
            $module0,
            ${mappings.Expr(dep.version)},
            CovariantSet(${Varargs(excludes)}: _*),
            ${mappings.mapStringStringOption(dep.userParams)}
          )
        }
      case (false, _, _, _) =>
        val module0 = noAttrModule(dep.module, mappings)
        val excludes = dep.exclude.toVector.sortBy(_.toString).map(simpleModule(_, mappings))
        '{
          DependencyLike[NoAttributes.type, NameAttributes](
            $module0,
            ${mappings.Expr(dep.version)},
            CovariantSet(${Varargs(excludes)}: _*),
            ${mappings.mapStringStringOption(dep.userParams)}
          )
        }
      case (true, false, _, _) | (true, true, true, _) =>
        val module0 = scalaAttrModule(dep.module, mappings)
        val excludes = dep.exclude.toVector.sortBy(_.toString).map(noAttrModule(_, mappings))
        '{
          DependencyLike[ScalaNameAttributes, NoAttributes.type](
            $module0,
            ${mappings.Expr(dep.version)},
            CovariantSet(${Varargs(excludes)}: _*),
            ${mappings.mapStringStringOption(dep.userParams)}
          )
        }
      case (true, true, false, true) =>
        val module0 = scalaAttrModule(dep.module, mappings)
        val excludes = dep.exclude.toVector.sortBy(_.toString).map(scalaAttrModule(_, mappings))
        '{
          DependencyLike[ScalaNameAttributes, ScalaNameAttributes](
            $module0,
            ${mappings.Expr(dep.version)},
            CovariantSet(${Varargs(excludes)}: _*),
            ${mappings.mapStringStringOption(dep.userParams)}
          )
        }
      case (true, _, _, _) =>
        val module0 = scalaAttrModule(dep.module, mappings)
        val excludes = dep.exclude.toVector.sortBy(_.toString).map(simpleModule(_, mappings))
        '{
          DependencyLike[ScalaNameAttributes, NameAttributes](
            $module0,
            ${mappings.Expr(dep.version)},
            CovariantSet(${Varargs(excludes)}: _*),
            ${mappings.mapStringStringOption(dep.userParams)}
          )
        }
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
    transparent inline def mod(inline args: Any*): ModuleLike[NameAttributes] =
      ${Extensions.parseModule('sc, 'args)}
    transparent inline def dep(inline args: Any*): DependencyLike[NameAttributes, NameAttributes] =
      ${Extensions.parseDependency('sc, 'args)}
}
