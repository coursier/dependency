package dependency

import com.eed3si9n.expecty.Expecty.expect

class ScalaVersionTests extends munit.FunSuite {

  test("scala 2 release") {
    val sv = "2.13.5"
    val sbv = ScalaVersion.binary(sv)
    val expected = "2.13"
    expect(sbv == expected)
  }
  test("scala 2 snapshot") {
    val sv = "2.13.6-SNAPSHOT"
    val sbv = ScalaVersion.binary(sv)
    val expected = "2.13"
    expect(sbv == expected)
  }
  test("scala 2 nightly") {
    val sv = "2.13.5-bin-aab85b1"
    val sbv = ScalaVersion.binary(sv)
    val expected = "2.13"
    expect(sbv == expected)
  }

  test("dotty") {
    val sv = "0.27.3"
    val sbv = ScalaVersion.binary(sv)
    val expected = "0.27"
    expect(sbv == expected)
  }
  test("early scala 3 RC") {
    val sv = "3.0.0-RC2"
    val sbv = ScalaVersion.binary(sv)
    val expected = "3.0.0-RC2"
    expect(sbv == expected)
  }
  test("early scala 3 nightly") {
    val sv = "3.0.0-RC2-bin-20210323-d4f1c26-NIGHTLY"
    val sbv = ScalaVersion.binary(sv)
    val expected = "3.0.0-RC2"
    expect(sbv == expected)
  }

  test("scala 3.0.0 final") {
    val sv = "3.0.0"
    val sbv = ScalaVersion.binary(sv)
    val expected = "3"
    expect(sbv == expected)
  }
  test("scala 3 release") {
    val sv = "3.0.1"
    val sbv = ScalaVersion.binary(sv)
    val expected = "3"
    expect(sbv == expected)
  }
  test("scala 3 RC") {
    val sv = "3.0.2-RC4"
    val sbv = ScalaVersion.binary(sv)
    val expected = "3"
    expect(sbv == expected)
  }
  test("scala 3 nightly") {
    val sv = "3.0.1-RC1-bin-20210405-16776c8-NIGHTLY"
    val sbv = ScalaVersion.binary(sv)
    val expected = "3"
    expect(sbv == expected)
  }

  test("typelevel") {
    val sv = "2.11.12-bin-typelevel.foo"
    val sbv = ScalaVersion.binary(sv)
    val expected = "2.11"
    expect(sbv == expected)
  }

  test("Scala.JS 0.6") {
    val ver = "0.6.29"
    val bin = ScalaVersion.jsBinary(ver)
    val expected = Some("0.6")
    expect(bin == expected)
  }
  test("Scala.JS pre 1.0") {
    val ver = "1.0.0-M3"
    val bin = ScalaVersion.jsBinary(ver)
    val expected = Some("1.0-M3")
    expect(bin == expected)
  }
  test("Scala.JS 1.0") {
    val ver = "1.0.1"
    val bin = ScalaVersion.jsBinary(ver)
    val expected = Some("1")
    expect(bin == expected)
  }
  test("Scala.JS 1.x") {
    val ver = "1.5.1"
    val bin = ScalaVersion.jsBinary(ver)
    val expected = Some("1")
    expect(bin == expected)
  }

  test("Scala Native 0.3.9") {
    val ver = "0.3.9"
    val bin = ScalaVersion.nativeBinary(ver)
    val expected = Some("0.3")
    expect(bin == expected)
  }
  test("Scala Native 0.4.0-M2") {
    val ver = "0.4.0-M2"
    val bin = ScalaVersion.nativeBinary(ver)
    val expected = Some("0.4.0-M2")
    expect(bin == expected)
  }
  test("Scala Native 0.4.0") {
    val ver = "0.4.0"
    val bin = ScalaVersion.nativeBinary(ver)
    val expected = Some("0.4")
    expect(bin == expected)
  }

}
