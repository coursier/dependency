import mill._, scalalib._

object Scala {
  def scala212 = "2.12.17"
  def scala213 = "2.13.10"
  def scala3 = "3.2.1"
  def all = Seq(scala212, scala213, scala3)
}

object Deps {
  def expecty = ivy"com.eed3si9n.expecty::expecty:0.16.0"
  def munit = ivy"org.scalameta::munit:1.0.0-M7"
  def scalaReflect(sv: String) = ivy"org.scala-lang:scala-reflect:$sv"
}

object Versions {
  def mdoc = "2.3.6"
}
