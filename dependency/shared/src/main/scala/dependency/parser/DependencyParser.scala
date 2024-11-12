package dependency
package parser

object DependencyParser {

  private def parseExclude(input: String): Either[String, AnyModule] =
    input.split("%", -1) match {
      case Array(org, "", name) if org.nonEmpty && name.nonEmpty => Right(ScalaModule(org, name))
      case Array(org, name) if org.nonEmpty && name.nonEmpty => Right(Module(org, name))
      case _ => Left(s"Unrecognized excluded module: '$input' (expected: org%name or org%%name)")
    }

  private def parseParam(input: String): (String, Option[String]) =
    input.split("=", 2) match {
      // the match is total
      case Array(k)    => (k, None)
      case Array(k, v) => (k, Some(v))
    }

  def parse(input: String): Either[String, AnyDependency] =
    parse(input, acceptInlineConfiguration = true)

  def parse(input: String, acceptInlineConfiguration: Boolean): Either[String, AnyDependency] =
    ModuleParser.parsePrefix(input).flatMap {
      case (module, remaining) =>
        val (version, configOpt, params) = splitRemainingPart(remaining, acceptInlineConfiguration)
        assert(acceptInlineConfiguration || configOpt.isEmpty)

        val (excludeParams, remainingParams) = params.partition(_.startsWith("exclude="))
        val maybeExclusions = excludeParams
          .iterator
          .map(_.stripPrefix("exclude="))
          .foldLeft[Either[String, CovariantSet[AnyModule]]](Right(CovariantSet())) { (eitherAcc, input) =>
            for {
              acc <- eitherAcc
              elem <- parseExclude(input)
            } yield acc += elem
          }

        for {
          exclusions <- maybeExclusions
        } yield {
          val userParams = remainingParams.iterator.map(parseParam).toSeq ++ configOpt.toSeq.map("$inlineConfiguration" -> Some(_))
          DependencyLike(module, version, exclusions, userParams)
        }
    }

  private def attrSeparator = ","
  private def argSeparator = ":"

  private def splitRemainingPart(input: String, acceptInlineConfiguration: Boolean): (String, Option[String], Seq[String]) = {

    def simpleSplit(s: String): (String, Seq[String]) =
      s.split(attrSeparator) match {
        case Array(coordsEnd, attrs @ _*) => (coordsEnd, attrs)
      }

    val splitAtOpt =
      if (input.startsWith("[") || input.startsWith("(")) {
        val idx = input.indexWhere(c => c == ']' || c == ')')
        Some(idx + 1).filter(_ >= 1)
      }
      else None

    val (versionPart, attrs0) = splitAtOpt match {
      case None => simpleSplit(input)
      case Some(idx) =>
        val (ver, attrsPart) = input.splitAt(idx)
        val (coordsEnd, attrs) = simpleSplit(attrsPart)
        (ver + coordsEnd, attrs)
    }

    if (acceptInlineConfiguration)
      versionPart.split(argSeparator, 2) match {
        case Array(ver, config) => (ver, Some(config), attrs0)
        case Array(ver) => (ver, None, attrs0)
      }
    else
      (versionPart, None, attrs0)
  }

}
