package dependency
package parser

import com.eed3si9n.expecty.Expecty.expect

class ModuleParserTests extends munit.FunSuite {

  test("simple") {
    val res = ModuleParser.parse("org:name")
    val expected = Right(Module("org", "name"))
    expect(res == expected)
  }

  test("scala") {
    val res = ModuleParser.parse("org::name")
    val expected = Right(ScalaModule("org", "name"))
    expect(res == expected)
  }

  test("scala full cross-version") {
    val res = ModuleParser.parse("org:::name")
    val expected = Right(ScalaModule("org", "name", fullCrossVersion = true))
    expect(res == expected)
  }

  test("attributes") {
    val res = ModuleParser.parse("org:name;scala=2.12;sbt=1.0")
    val expected = Right(Module("org", "name", Map("scala" -> "2.12", "sbt" -> "1.0")))
    expect(res == expected)
  }

  test("attributes in scala") {
    val res = ModuleParser.parse("org::name;scala=2.12;sbt=1.0")
    val expected = Right(ScalaModule("org", "name").copy(attributes = Map("scala" -> "2.12", "sbt" -> "1.0")))
    expect(res == expected)
  }

  test("attributes in scala full cross-version") {
    val res = ModuleParser.parse("org:::name;scala=2.12;sbt=1.0")
    val expected = Right(ScalaModule("org", "name", fullCrossVersion = true).copy(attributes = Map("scala" -> "2.12", "sbt" -> "1.0")))
    expect(res == expected)
  }


  test("empty org") {
    val res = ModuleParser.parse(":name")
    expect(res.isLeft)
  }

  test("empty org with attributes") {
    val res = ModuleParser.parse(":name;scala=2.12;sbt=1.0")
    expect(res.isLeft)
  }

  test("empty org scala") {
    val res = ModuleParser.parse("::name")
    expect(res.isLeft)
  }

  test("empty org scala full cross-version") {
    val res = ModuleParser.parse(":::name")
    expect(res.isLeft)
  }

  test("empty org and garbage") {
    val res = ModuleParser.parse(":name:1.2")
    expect(res.isLeft)
  }

  test("empty name") {
    val res = ModuleParser.parse("org:")
    expect(res.isLeft)
  }

  test("empty scala name") {
    val res = ModuleParser.parse("org::")
    expect(res.isLeft)
  }

  test("empty scala name full cross-version") {
    val res = ModuleParser.parse("org:::")
    expect(res.isLeft)
  }

  test("reject version") {
    val res = ModuleParser.parse("org:name:1.2")
    expect(res.isLeft)
  }

  test("reject version, scala") {
    val res = ModuleParser.parse("org::name:1.2")
    expect(res.isLeft)
  }

}
