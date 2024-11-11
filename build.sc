import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import $file.deps, deps.{Deps, Scala, Versions}

import java.io.File
import de.tobiasroeser.mill.vcs.version._
import mill._, scalalib._
import scala.concurrent.duration.DurationInt

object dependency extends Cross[Dependency](Scala.all)
object `dependency-interface` extends Cross[DependencyInterface](Scala.all)

trait DependencyPublishModule extends PublishModule {

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

  def publishVersion = T {
    val state = VcsVersion.vcsState()
    if (state.commitsSinceLastTag > 0) {
      val versionOrEmpty = state.lastTag
        .filter(_ != "latest")
        .map(_.stripPrefix("v"))
        .flatMap { tag =>
          val idx = tag.lastIndexOf(".")
          if (idx >= 0)
            Some(tag.take(idx + 1) + (tag.drop(idx + 1).takeWhile(_.isDigit).toInt + 1).toString + "-SNAPSHOT")
          else None
        }
        .getOrElse("0.0.1-SNAPSHOT")
      Some(versionOrEmpty)
        .filter(_.nonEmpty)
        .getOrElse(state.format())
    } else
      state
        .lastTag
        .getOrElse(state.format())
        .stripPrefix("v")
  }
}

trait Dependency extends CrossSbtModule with DependencyPublishModule {

  def compileIvyDeps = T{
    val sv = scalaVersion()
    if (sv.startsWith("2.")) Agg(Deps.scalaReflect(sv))
    else Agg.empty[Dep]
  }
  def scalacOptions = super.scalacOptions() ++ Seq("-release", "8")
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
  def scalacOptions = super.scalacOptions() ++ Seq("-release", "8")
  object test extends Tests with TestModule.Munit {
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munit
    )
  }
}

def readme = T.sources {
  Seq(PathRef(T.workspace / "README.md"))
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
    cwd = T.workspace,
    stdin = os.Inherit,
    stdout = os.Inherit,
    stderr = os.Inherit
  )
}

def publishSonatype(tasks: mill.main.Tasks[PublishModule.PublishData]) =
  T.command {
    import scala.concurrent.duration._

    val data = T.sequence(tasks.value)()
    val log  = T.ctx().log

    val credentials = sys.env("SONATYPE_USERNAME") + ":" + sys.env("SONATYPE_PASSWORD")
    val pgpPassword = sys.env("PGP_PASSPHRASE")
    val timeout     = 10.minutes

    val artifacts = data.map {
      case PublishModule.PublishData(a, s) =>
        (s.map { case (p, f) => (p.path, f) }, a)
    }

    val isRelease = {
      val versions = artifacts.map(_._2.version).toSet
      val set      = versions.map(!_.endsWith("-SNAPSHOT"))
      assert(
        set.size == 1,
        s"Found both snapshot and non-snapshot versions: ${versions.toVector.sorted.mkString(", ")}",
      )
      set.head
    }
    val publisher = new scalalib.publish.SonatypePublisher(
      uri = "https://oss.sonatype.org/service/local",
      snapshotUri = "https://oss.sonatype.org/content/repositories/snapshots",
      credentials = credentials,
      signed = true,
      gpgArgs = Seq(
        "--detach-sign",
        "--batch=true",
        "--yes",
        "--pinentry-mode",
        "loopback",
        "--passphrase",
        pgpPassword,
        "--armor",
        "--use-agent",
      ),
      readTimeout = timeout.toMillis.toInt,
      connectTimeout = timeout.toMillis.toInt,
      log = log,
      workspace = T.workspace,
      env = Map.empty,
      awaitTimeout = timeout.toMillis.toInt,
      stagingRelease = isRelease,
    )

    publisher.publishAll(isRelease, artifacts: _*)
  }
