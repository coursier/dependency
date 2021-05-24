import mill._, scalalib._

object Scala {
  def scala212 = "2.12.13"
  def scala213 = "2.13.6"
  def scala3 = "3.0.0"
  def all = Seq(scala212, scala213, scala3)
}

object Deps {
  def expecty = ivy"com.eed3si9n.expecty::expecty:0.15.3"
  def munit = ivy"org.scalameta::munit:0.7.26"
  def scalaReflect(sv: String) = ivy"org.scala-lang:scala-reflect:$sv"
}

object Versions {
  def mdoc = "2.2.12"
}
