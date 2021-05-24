import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import $file.deps, deps.{Deps, Scala, Versions}
import $file.settings, settings.DependencyPublishModule

import java.io.File
import mill._, scalalib._
import scala.concurrent.duration.DurationInt

object dependency extends Cross[Dependency](Scala.all: _*)

class Dependency(val crossScalaVersion: String) extends CrossSbtModule with DependencyPublishModule {
  def compileIvyDeps = T{
    val sv = scalaVersion()
    if (sv.startsWith("2.")) Agg(Deps.scalaReflect(sv))
    else Agg.empty[Dep]
  }
  object test extends Tests {
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munit
    )
    def testFramework = "munit.Framework"
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

def publishSonatype(tasks: mill.main.Tasks[PublishModule.PublishData]) =
  T.command {
    val timeout = 10.minutes
    val credentials = sys.env("SONATYPE_USERNAME") + ":" + sys.env("SONATYPE_PASSWORD")
    val pgpPassword = sys.env("PGP_PASSPHRASE")
    val data = define.Task.sequence(tasks.value)()

    settings.publishSonatype(
      credentials = credentials,
      pgpPassword = pgpPassword,
      data = data,
      timeout = timeout,
      log = T.ctx().log
    )
  }