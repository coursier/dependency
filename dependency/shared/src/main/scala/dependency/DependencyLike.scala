package dependency

final case class DependencyLike[+A <: NameAttributes, +E <: NameAttributes](
  module: ModuleLike[A],
  version: String,
  exclude: CovariantSet[ModuleLike[E]],
  userParams: Seq[(String, Option[String])]
) {

  ModuleLike.validateValue(version, "version")

  def applyParams(params: ScalaParameters): Dependency =
    DependencyLike(
      module.applyParams(params),
      version,
      exclude.map(_.applyParams(params)),
      userParams
    )

  def organization: String = module.organization
  def name: String = module.name
  def nameAttributes: A = module.nameAttributes
  def attributes: Map[String, String] = module.attributes

  lazy val userParamsMap: Map[String, Seq[Option[String]]] = userParams
    .groupBy(_._1)
    .mapValues(_.map(_._2))
    .toMap

  private def excludeString: String =
    exclude.toVector.map(",exclude=" + _.render("%")).sorted.mkString
  private def userParamsString: String =
    userParams
      .map {
        case (k, None) => k
        case (k, Some(v)) => s"$k=$v"
      }
      .sorted
      .map("," + _)
      .mkString
  private def paramsString: String =
    excludeString ++ userParamsString
  def render: String =
    s"${module.render}:$version$paramsString"
  override def toString: String =
    render
}

object DependencyLike {
  def apply[A <: NameAttributes, E <: NameAttributes](
    module: ModuleLike[A],
    version: String,
    exclude: CovariantSet[ModuleLike[E]]
  ): DependencyLike[A, E] =
    DependencyLike[A, E](
      module,
      version,
      exclude,
      Nil
    )

  def apply[A <: NameAttributes](
    module: ModuleLike[A],
    version: String
  ): DependencyLike[A, NameAttributes] =
    DependencyLike[A, NameAttributes](
      module,
      version,
      CovariantSet.empty[AnyModule],
      Nil
    )
}
