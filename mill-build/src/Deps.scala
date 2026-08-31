package millbuild

import mill.*
import mill.scalalib.*

object Scala {
  def scala212 = "2.12.20"
  def scala213 = "2.13.15"
  def scala3 = "3.3.4"
  def all = Seq(scala212, scala213, scala3)
}

object Deps {
  def expecty = mvn"com.eed3si9n.expecty::expecty::0.17.0"
  def interface = mvn"io.get-coursier:interface:1.0.28"
  def munit = mvn"org.scalameta::munit::1.0.4"
  def pprint = mvn"com.lihaoyi::pprint::0.9.0"
  def scalaReflect(sv: String) = mvn"org.scala-lang:scala-reflect:$sv"
}

object Versions {
  def mdoc = "2.3.6"
  def scalaJs = "1.17.0"
  def scalaNative = "0.5.12"
}
