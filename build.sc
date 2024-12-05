import $ivy.`com.github.lolgab::mill-mima::0.0.24`
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import $file.deps, deps.{Deps, Scala, Versions}

import java.io.File
import com.github.lolgab.mill.mima.Mima
import de.tobiasroeser.mill.vcs.version._
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalanativelib._
import scala.concurrent.duration.DurationInt

object dependency extends Module {
  object jvm extends Cross[DependencyJvm](Scala.all)
  object js extends Cross[DependencyJs](Scala.all)
  object native extends Cross[DependencyNative](Scala.all)
}
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

trait DependencyMima extends Mima {
  def mimaPreviousVersions: T[Seq[String]] = T.input {
    val current = os.proc("git", "describe", "--tags", "--match", "v*")
      .call(cwd = T.workspace)
      .out.trim()
    os.proc("git", "tag", "-l")
      .call(cwd = T.workspace)
      .out.lines()
      .filter(_ != current)
      .filter(_.startsWith("v"))
      .filter(!_.contains("-"))
      .map(_.stripPrefix("v"))
      .filter(!_.startsWith("0.0."))
      .filter(!_.startsWith("0.1."))
      .filter(!_.startsWith("0.2."))
      .map(coursier.core.Version(_))
      .sorted
      .map(_.repr)
  }
  // Remove once 0.3.0 is out
  def mimaPreviousArtifacts = T {
    val versions     = mimaPreviousVersions()
    val organization = pomSettings().organization
    val artifactId0  = artifactId()
    Agg.from(
      versions.map(version => ivy"$organization:$artifactId0:$version")
    )
  }
}

private def scalaDirNames(sv: String): Seq[String] = {
  val split = sv.split('.')
  val major = split.head
  val sbv = split.take(2).mkString(".")
  Seq("scala", s"scala-$major", s"scala-$sbv", s"scala-$sv")
}

trait Dependency extends CrossSbtModule with DependencyPublishModule {
  def sources = T.sources {
    super.sources() ++ scalaDirNames(scalaVersion()).map(T.workspace / "dependency" / "shared" / "src" / "main" / _).map(PathRef(_))
  }
  def compileIvyDeps = T{
    val sv = scalaVersion()
    if (sv.startsWith("2.")) Agg(Deps.scalaReflect(sv))
    else Agg.empty[Dep]
  }
  def scalacOptions = super.scalacOptions() ++ Seq("-release", "8")
}

trait DependencyJvm extends Dependency with DependencyMima {
  def artifactName = "dependency"
  object test extends CrossSbtTests with TestModule.Munit {
    def sources = T.sources {
      super.sources() ++ scalaDirNames(scalaVersion()).map(T.workspace / "dependency" / "shared" / "src" / "test" / _).map(PathRef(_))
    }
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munit,
      Deps.pprint
    )
  }
}

trait DependencyJs extends Dependency with ScalaJSModule {
  def artifactName = "dependency"
  def scalaJSVersion = Versions.scalaJs
  object test extends CrossSbtTests with ScalaJSTests with TestModule.Munit {
    def sources = T.sources {
      super.sources() ++ scalaDirNames(scalaVersion()).map(T.workspace / "dependency" / "shared" / "src" / "test" / _).map(PathRef(_))
    }
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munit,
      Deps.pprint
    )
  }
}

trait DependencyNative extends Dependency with ScalaNativeModule {
  def artifactName = "dependency"
  def scalaNativeVersion = Versions.scalaNative
  object test extends CrossSbtTests with ScalaNativeTests with TestModule.Munit {
    def sources = T.sources {
      super.sources() ++ scalaDirNames(scalaVersion()).map(T.workspace / "dependency" / "shared" / "src" / "test" / _).map(PathRef(_))
    }
    def ivyDeps = Agg(
      Deps.expecty,
      Deps.munitForNative04,
      Deps.pprintForNative04
    )
  }
}


trait DependencyInterface extends CrossSbtModule with DependencyPublishModule {

  def moduleDeps = super.moduleDeps ++ Seq(
    dependency.jvm()
  )

  def ivyDeps = super.ivyDeps() ++ Agg(
    Deps.interface
  )
  def scalacOptions = super.scalacOptions() ++ Seq("-release", "8")
  object test extends CrossSbtTests with TestModule.Munit {
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
  val cp = (dependency.jvm(mdocScalaVersion).runClasspath() :+ dependency.jvm(mdocScalaVersion).jar())
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
