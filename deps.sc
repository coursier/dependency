import mill._, scalalib._

object Scala {
  def scala212 = "2.12.20"
  def scala213 = "2.13.15"
  def scala3 = "3.3.4"
  def all = Seq(scala212, scala213, scala3)
}

object Deps {
  def expecty = ivy"com.eed3si9n.expecty::expecty:0.16.0"
  def interface = ivy"io.get-coursier:interface:1.0.21"
  def munit = ivy"org.scalameta::munit:1.0.2"
  def scalaReflect(sv: String) = ivy"org.scala-lang:scala-reflect:$sv"
}

object Versions {
  def mdoc = "2.3.6"
}
