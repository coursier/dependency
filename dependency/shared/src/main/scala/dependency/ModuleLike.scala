package dependency

final case class ModuleLike[+A <: NameAttributes](
  organization: String,
  name: String,
  nameAttributes: A,
  attributes: Map[String, String]
) {

  ModuleLike.validateValue(organization, "organization")
  ModuleLike.validateValue(name, "module name")

  def applyParams(params: ScalaParameters): Module =
    copy(
      name = name + nameAttributes.suffix(params),
      nameAttributes = NoAttributes
    )

  private def attributesString: String =
    attributes.toVector.sorted.map { case (k, v) => s";$k=$v" }.mkString
  def render: String =
    render(":")
  def render(separator: String): String =
    s"$organization$separator${nameAttributes.render(name, separator)}$attributesString"
  override def toString: String =
    render
}

object ModuleLike {
  private[dependency] def validateValue(value: String, name: String): Unit =
    if (value.contains("/")) throw new IllegalArgumentException(s"$name $value contains invalid '/'")
    else if (value.contains("\\")) throw new IllegalArgumentException(s"$name $value contains invalid '\\'")
}
