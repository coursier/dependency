package dependency.api

object ops {

  implicit class ModuleOps(private val mod: dependency.Module) extends AnyVal {
    def toCs: coursierapi.Module =
      ApiConverter.module(mod)
  }
  implicit class DependencyOps(private val dep: dependency.Dependency) extends AnyVal {
    def toCs: coursierapi.Dependency =
      ApiConverter.dependency(dep)
  }

}
