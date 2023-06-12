import $ivy.`io.chris-kipp::mill-ci-release::0.1.9`
import $file.deps, deps.{Deps, Scala, Versions}

import java.io.File
import mill._, scalalib._
import scala.concurrent.duration.DurationInt
import io.kipp.mill.ci.release.CiReleaseModule

object dependency extends Cross[Dependency](Scala.all)
object `dependency-interface` extends Cross[DependencyInterface](Scala.all)

trait DependencyPublishModule extends CiReleaseModule {

  import mill.scalalib.publish._

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "io.get-coursier",
    url = "https://github.com/coursier/dependency",
    licenses = Seq(License.`BSD-3-Clause`),
    versionControl = VersionControl.github("coursier", "dependency"),
    developers = Seq(
      Developer("alexarchambault", "Alex Archambault","https://github.com/alexarchambault")
    )
  )

}

trait Dependency extends CrossSbtModule with DependencyPublishModule {

  def compileIvyDeps = T{
    val sv = scalaVersion()
    if (sv.startsWith("2.")) Agg(Deps.scalaReflect(sv))
    else Agg.empty[Dep]
  }
  object test extends Tests with TestModule.Munit {
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munit
    )
  }
}


trait DependencyInterface extends CrossSbtModule with DependencyPublishModule {

  def moduleDeps = super.moduleDeps ++ Seq(
    dependency()
  )

  def ivyDeps = super.ivyDeps() ++ Agg(
    Deps.interface
  )
  object test extends Tests with TestModule.Munit {
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munit
    )
  }
}

def readme = T.sources {
  Seq(PathRef(os.pwd / "README.md"))
}

private def mdocScalaVersion = Scala.scala213
def mdoc(args: String*) = T.command {
  val readme0 = readme().head.path
  val dest = T.dest / "README.md"
  val cp = (dependency(mdocScalaVersion).runClasspath() :+ dependency(mdocScalaVersion).jar())
    .map(_.path)
    .filter(os.exists(_))
    .filter(!os.isDir(_))
  val cmd = Seq("cs", "launch", s"mdoc:${Versions.mdoc}", "--scala", mdocScalaVersion)
  val mdocArgs = Seq(
    "--in", readme0.toString,
    "--out", dest.toString,
    "--classpath", cp.mkString(File.pathSeparator)
  )
  os.proc(cmd, "--", mdocArgs, args).call(
    stdin = os.Inherit,
    stdout = os.Inherit,
    stderr = os.Inherit
  )
}
