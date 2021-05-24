package dependency

final case class ModuleLike[+A <: NameAttributes](
  organization: String,
  name: String,
  nameAttributes: A,
  attributes: Map[String, String]
) {

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
