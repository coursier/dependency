package dependency

sealed abstract class NameAttributes extends Product with Serializable {
  def suffix(params: ScalaParameters): String
  def render(name: String, separator: String): String
  final def render(name: String): String = render(name, ":")
}

final case class ScalaNameAttributes(
  fullCrossVersion: Option[Boolean],
  platform: Option[Boolean]
) extends NameAttributes {

  def platformSuffix(params: ScalaParameters): String =
    if (platform.getOrElse(false)) params.platform.fold("")("_" + _)
    else ""

  def versionSuffix(params: ScalaParameters): String = {
    val value =
      if (fullCrossVersion.getOrElse(false)) params.scalaVersion
      else params.scalaBinaryVersion
    "_" + value
  }

  def suffix(params: ScalaParameters): String =
    platformSuffix(params) + versionSuffix(params)
  def render(name: String, separator: String): String = {
    val prefix = if (fullCrossVersion.contains(true)) separator * 2 else separator
    val suffix = if (platform.contains(true)) separator else ""
    prefix + name + suffix
  }
}

case object NoAttributes extends NameAttributes {
  def suffix(params: ScalaParameters): String = ""
  def render(name: String, separator: String): String = name
}
