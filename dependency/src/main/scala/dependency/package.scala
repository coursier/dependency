
package object dependency extends dependency.literal.Extensions {

  type Module = ModuleLike[NoAttributes.type]
  type Dependency = DependencyLike[NoAttributes.type, NoAttributes.type]

  type ScalaModule = ModuleLike[ScalaNameAttributes]
  type ScalaDependency = DependencyLike[ScalaNameAttributes, NameAttributes]

  type AnyModule = ModuleLike[NameAttributes]
  type AnyDependency = DependencyLike[NameAttributes, NameAttributes]

  object Module {
    def apply(
      organization: String,
      name: String,
      attributes: Map[String, String]
    ): Module =
      ModuleLike(
        organization,
        name,
        NoAttributes,
        attributes
      )

    def apply(
      organization: String,
      name: String
    ): Module =
      ModuleLike(
        organization,
        name,
        NoAttributes,
        Map()
      )
  }

  object Dependency {
    def apply(
      module: Module,
      version: String,
      exclude: CovariantSet[Module],
      userParams: Map[String, Option[String]]
    ): Dependency =
      DependencyLike(
        module,
        version,
        exclude,
        userParams
      )

    def apply(
      module: Module,
      version: String,
      exclude: CovariantSet[Module]
    ): Dependency =
      DependencyLike(
        module,
        version,
        exclude,
        Map()
      )

    def apply(
      module: Module,
      version: String
    ): Dependency =
      DependencyLike(
        module,
        version,
        CovariantSet(),
        Map()
      )

    def apply(
      organization: String,
      name: String,
      version: String
    ): Dependency =
      DependencyLike(
        Module(organization, name),
        version,
        CovariantSet(),
        Map()
      )
  }

  object ScalaModule {
    def apply(
      organization: String,
      name: String,
      fullCrossVersion: Boolean,
      platform: Boolean,
      attributes: Map[String, String]
    ): ScalaModule =
      ModuleLike(
        organization,
        name,
        ScalaNameAttributes(
          if (fullCrossVersion) Some(true) else None,
          if (platform) Some(true) else None
        ),
        attributes
      )

    def apply(
      organization: String,
      name: String,
      fullCrossVersion: Boolean,
      platform: Boolean
    ): ScalaModule =
      ModuleLike(
        organization,
        name,
        ScalaNameAttributes(
          if (fullCrossVersion) Some(true) else None,
          if (platform) Some(true) else None
        ),
        Map()
      )

    def apply(
      organization: String,
      name: String,
      fullCrossVersion: Boolean
    ): ScalaModule =
      ModuleLike(
        organization,
        name,
        ScalaNameAttributes(
          if (fullCrossVersion) Some(true) else None,
          None
        ),
        Map()
      )

    def apply(
      organization: String,
      name: String
    ): ScalaModule =
      ModuleLike(
        organization,
        name,
        ScalaNameAttributes(
          None,
          None
        ),
        Map()
      )
  }

  object ScalaDependency {
    def apply(
      module: ScalaModule,
      version: String,
      exclude: CovariantSet[AnyModule],
      userParams: Map[String, Option[String]]
    ): ScalaDependency =
      DependencyLike(
        module,
        version,
        exclude,
        userParams
      )

    def apply(
      module: ScalaModule,
      version: String,
      exclude: CovariantSet[AnyModule]
    ): ScalaDependency =
      DependencyLike(
        module,
        version,
        exclude,
        Map()
      )

    def apply(
      module: ScalaModule,
      version: String
    ): ScalaDependency =
      DependencyLike(
        module,
        version,
        CovariantSet(),
        Map()
      )

    def apply(
      organization: String,
      name: String,
      version: String
    ): ScalaDependency =
      DependencyLike(
        ScalaModule(organization, name),
        version,
        CovariantSet(),
        Map()
      )
  }
}
