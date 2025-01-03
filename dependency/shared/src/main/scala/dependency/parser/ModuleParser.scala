package dependency
package parser

object ModuleParser {

  private[parser] def paramSeparator = ","
  private[parser] def argSeparator = ":"

  private implicit class EitherWithFilter[L, R](private val e: Either[L, R]) extends AnyVal {
    def withFilter(f: R => Boolean): Either[L, R] =
      if (e.forall(f)) e else throw new MatchError(e)
  }

  /**
    * Parses a module like
    *   org:name
    *  possibly with attributes, like
    *    org:name;attr1=val1;attr2=val2
    *
    * Two semi-columns after the org part is interpreted as a scala module. E.g. if
    * the scala version is 2.13., org::name is equivalent to org:name_2.13.
    */
  def parse(input: String): Either[String, AnyModule] = {

    val parts = input.split(argSeparator, -1).map(Some(_).filter(_.nonEmpty))

    val values = parts match {
      case Array(Some(org), Some(name))                             => Right((org, name, NoAttributes))
      case Array(Some(org), None,     Some(name))                   => Right((org, name, ScalaNameAttributes(None, None)))
      case Array(Some(org), None,     Some(name), None)             => Right((org, name, ScalaNameAttributes(None, Some(true))))
      case Array(Some(org), None,     None,       Some(name))       => Right((org, name, ScalaNameAttributes(Some(true),  None)))
      case Array(Some(org), None,     None,       Some(name), None) => Right((org, name, ScalaNameAttributes(Some(true),  Some(true))))
      case _ => Left(s"malformed module: $input")
    }

    for {
      (org, name0, nameAttributes) <- values
      _ <- validateValue(org, "organization")
      (name, attributes) <- parseNamePart(name0)
      _ <- validateValue(name, "module name")
    } yield ModuleLike(org, name, nameAttributes, attributes)
  }

  def parsePrefix(input: String): Either[String, (AnyModule, String)] = {

    val (input0, paramsOpt) = input.split(paramSeparator, 2) match {
      case Array(input0) => (input0, None)
      case Array(input0, params) => (input0, Some(params))
    }
    val parts = input0.split(argSeparator, -1).map(Some(_).filter(_.nonEmpty))

    val values = parts match {
      case Array(Some(org), Some(name), rest @ _*)                             => Right((org, name, NoAttributes, rest))
      case Array(Some(org), None,     Some(name), None, rest @ _*)             => Right((org, name, ScalaNameAttributes(None, Some(true)), rest))
      case Array(Some(org), None,     Some(name), rest @ _*)                   => Right((org, name, ScalaNameAttributes(None, None), rest))
      case Array(Some(org), None,     None,       Some(name), None, rest @ _*) => Right((org, name, ScalaNameAttributes(Some(true),  Some(true)), rest))
      case Array(Some(org), None,     None,       Some(name), rest @ _*)       => Right((org, name, ScalaNameAttributes(Some(true),  None), rest))
      case _ => Left(s"malformed module: $input")
    }

    for {
      (org, name0, nameAttributes, rest) <- values
      _ <- validateValue(org, "organization")
      (name, attributes) <- parseNamePart(name0)
      _ <- validateValue(name, "module name")
    } yield (ModuleLike(org, name, nameAttributes, attributes), rest.map(_.getOrElse("")).mkString(argSeparator) + paramsOpt.fold("")(paramSeparator + _))
  }

  private def parseNamePart(input: String): Either[String, (String, Map[String, String])] = {

    val split = input.split(';')
    val malformedAttrs = split.tail.exists(!_.contains("="))

    if (malformedAttrs)
      Left(s"malformed attribute(s) in $input")
    else {
      val name = split.head
      val attributes = split.tail.map(_.split("=", 2)).map {
        case Array(key, value) =>
          key -> value
      }
      Right((name, attributes.toMap))
    }
  }

  private[dependency] def validateValue(value: String, name: String): Either[String, Unit] =
    if (value.contains("/")) Left(s"$name $value contains invalid '/'")
    else if (value.contains("\\")) Left(s"$name $value contains invalid '\\'")
    else Right(())
}