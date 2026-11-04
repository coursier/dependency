package dependency
package literal

import scala.language.experimental.macros

trait Extensions {
  implicit class moduleString(val sc: StringContext) {
    def mod(args: String*): ModuleLike[NameAttributes] =
      macro ModuleLiteralMacros.module
  }
  implicit class dependencyString(val sc: StringContext) {
    def dep(args: String*): DependencyLike[NameAttributes, NameAttributes] =
      macro DependencyLiteralMacros.dependency
  }
}
