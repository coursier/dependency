package dependency

object ScalaVersion {

  // adapted from https://github.com/com-lihaoyi/mill/blob/5a62e25c031bc809386f6f9a9962154d990139b4/scalalib/api/src/ZincWorkerApi.scala#L83-L132

  private val PartialVersion       = raw"""(\d+)\.(\d+)\.*""".r
  private val ReleaseVersion       = raw"""(\d+)\.(\d+)\.(\d+)""".r
  private val MinorSnapshotVersion = raw"""(\d+)\.(\d+)\.([1-9]\d*)-SNAPSHOT""".r
  private val DottyVersion         = raw"""0\.(\d+)\.(\d+).*""".r
  private val Scala3EarlyVersion   = raw"""3\.0\.0-(\w+).*""".r
  private val Scala3Version        = raw"""3\.(\d+)\.(\d+).*""".r
  private val DottyNightlyVersion  = raw"""(0|3)\.(\d+)\.(\d+)-bin-(.*)-NIGHTLY""".r
  private val NightlyVersion       = raw"""(\d+)\.(\d+)\.(\d+)-bin-[a-f0-9]*""".r
  private val TypelevelVersion     = raw"""(\d+)\.(\d+)\.(\d+)-bin-typelevel.*""".r

  private val ScalaJsFullVersion     = """^([0-9]+)\.([0-9]+)\.([0-9]+)(-.*)?$""".r
  private val ScalaNativeFullVersion = """^([0-9]+)\.([0-9]+)\.([0-9]+)(-.*)?$""".r


  def binary(scalaVersion: String): String =
    scalaVersion match {
      case Scala3EarlyVersion(milestone)         => s"3.0.0-$milestone"
      case Scala3Version(_, _)                   => "3"
      case ReleaseVersion(major, minor, _)       => s"$major.$minor"
      case MinorSnapshotVersion(major, minor, _) => s"$major.$minor"
      case NightlyVersion(major, minor, _)       => s"$major.$minor"
      case DottyVersion(minor, _)                => s"0.$minor"
      case TypelevelVersion(major, minor, _)     => s"$major.$minor"
      case _                                     => scalaVersion
    }

  def jsBinary(scalaJsVersion: String): Option[String] =
    scalaJsVersion match {
      case _ if scalaJsVersion.startsWith("0.6.") =>
        Some("0.6")
      case ScalaJsFullVersion(major, "0", "0", suffix) if suffix != null =>
        Some(s"$major.0$suffix")
      case ScalaJsFullVersion(major, _, _, _) =>
        Some(major)
      case _ =>
        None
    }

  def nativeBinary(scalaNativeVersion: String): Option[String] =
    scalaNativeVersion match {
      case ScalaNativeFullVersion(_, _, "0", suffix) if suffix != null =>
        Some(scalaNativeVersion)
      case ScalaNativeFullVersion(major, minor, _, _) =>
        Some(s"$major.$minor")
      case _ =>
        None
    }
}
