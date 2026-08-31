package dependency

import scala.annotation.tailrec

/** Matches modules against a module whose organization, name, and attribute values can contain `*`
  * wildcards.
  *
  * `*` matches any sequence of characters, like in shell globs. Name attributes (whether a module is
  * a Scala module, is fully cross-versioned, has a platform suffix) are compared as is, unless
  * `matchNameAttributes` is `false`.
  */
final case class ModuleMatcher(
  matcher: AnyModule,
  matchNameAttributes: Boolean = true
) {

  import ModuleMatcher.blobMatches

  def matches(module: AnyModule): Boolean =
    blobMatches(matcher.organization, module.organization) &&
    blobMatches(matcher.name, module.name) &&
    (!matchNameAttributes || nameAttributesMatch(module.nameAttributes)) &&
    module.attributes.keySet == matcher.attributes.keySet &&
    matcher.attributes.forall {
      case (k, v) =>
        module.attributes.get(k).exists(blobMatches(v, _))
    }

  private def nameAttributesMatch(nameAttributes: NameAttributes): Boolean =
    (matcher.nameAttributes, nameAttributes) match {
      case (NoAttributes, NoAttributes) =>
        true
      case (a: ScalaNameAttributes, b: ScalaNameAttributes) =>
        a.fullCrossVersion.getOrElse(false) == b.fullCrossVersion.getOrElse(false) &&
        a.platform.getOrElse(false) == b.platform.getOrElse(false)
      case _ =>
        false
    }
}

object ModuleMatcher {

  def apply(organization: String, name: String): ModuleMatcher =
    ModuleMatcher(Module(organization, name))

  def apply(organization: String, name: String, attributes: Map[String, String]): ModuleMatcher =
    ModuleMatcher(Module(organization, name, attributes))

  /** A matcher accepting any module, whatever its name attributes */
  def all: ModuleMatcher =
    ModuleMatcher(Module("*", "*"), matchNameAttributes = false)

  /** Parses a matcher, like [[dependency.parser.ModuleParser.parse]] does, accepting `*` wildcards
    * in the organization, name, and attribute values
    */
  def parse(input: String): Either[String, ModuleMatcher] =
    parser.ModuleParser.parse(input).map(ModuleMatcher(_))

  /** Whether `value` matches `pattern`, where `*` in `pattern` matches any sequence of characters */
  def blobMatches(pattern: String, value: String): Boolean = {

    // Each part of the pattern is matched as early as possible in the value. That's fine, as
    // parts are separated by '*', that can match any number of characters.
    @tailrec
    def helper(pattern: String, value: String, anchored: Boolean): Boolean = {
      val idx = pattern.indexOf('*')
      if (idx < 0)
        if (anchored) pattern == value
        else value.endsWith(pattern)
      else {
        val prefix = pattern.substring(0, idx)
        val rest   = pattern.substring(idx + 1)
        val from   = if (anchored) (if (value.startsWith(prefix)) 0 else -1) else value.indexOf(prefix)
        from >= 0 && helper(rest, value.substring(from + prefix.length), anchored = false)
      }
    }

    helper(pattern, value, anchored = true)
  }
}
