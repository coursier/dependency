package dependency

final case class ScalaParameters(
  scalaVersion: String,
  scalaBinaryVersion: String,
  platform: Option[String]
)

object ScalaParameters {
  def apply(
    scalaVersion: String,
    scalaBinaryVersion: String
  ): ScalaParameters =
    ScalaParameters(scalaVersion, scalaBinaryVersion, None)

  def apply(scalaVersion: String): ScalaParameters =
    ScalaParameters(scalaVersion, ScalaVersion.binary(scalaVersion), None)
}
