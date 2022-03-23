package dependency
package parser

import com.eed3si9n.expecty.Expecty.expect

class DependencyParserTests extends munit.FunSuite {

  test("simple") {
    val res = DependencyParser.parse("org:name:1.2")
    val expected = Right(Dependency("org", "name", "1.2"))
    expect(res == expected)
  }

  test("scala") {
    val res = DependencyParser.parse("org::name:1.2")
    val expected = Right(ScalaDependency("org", "name", "1.2"))
    expect(res == expected)
  }

  test("scala full cross-version") {
    val res = DependencyParser.parse("org:::name:1.2")
    val expected = Right(ScalaDependency(ScalaModule("org", "name", fullCrossVersion = true), "1.2"))
    expect(res == expected)
  }

  test("scala platform") {
    val res = DependencyParser.parse("org::name::1.2")
    val expected = Right(ScalaDependency(ScalaModule("org", "name", fullCrossVersion = false, platform = true), "1.2"))
    expect(res == expected)
  }

  test("scala platform full cross-version") {
    val res = DependencyParser.parse("org:::name::1.2")
    val expected = Right(ScalaDependency(ScalaModule("org", "name", fullCrossVersion = true, platform = true), "1.2"))
    expect(res == expected)
  }

  test("scala with attributes") {
    val res = DependencyParser.parse("org::name;scala=2.12;sbt=1.0:1.2")
    val expected = Right(ScalaDependency(ScalaModule("org", "name").copy(attributes = Map("scala" -> "2.12", "sbt" -> "1.0")), "1.2"))
    expect(res == expected)
  }

  test("attributes") {
    val res = DependencyParser.parse("org:name;scala=2.12;sbt=1.0:1.2")
    val expected = Right(Dependency(Module("org", "name").copy(attributes = Map("scala" -> "2.12", "sbt" -> "1.0")), "1.2"))
    expect(res == expected)
  }

  test("exclude") {
    val res = DependencyParser.parse("org:name:1.2,exclude=fu%ba")
    val expected = Right(Dependency("org", "name", "1.2").copy(exclude = CovariantSet(Module("fu", "ba"))))
    expect(res == expected)
  }

  test("scala exclude") {
    val res = DependencyParser.parse("org:name:1.2,exclude=fu%%ba")
    val expected = Right(Dependency("org", "name", "1.2").copy(exclude = CovariantSet(ScalaModule("fu", "ba"))))
    expect(res == expected)
  }

  test("several exclude") {
    val res = DependencyParser.parse("org:name:1.2,exclude=fu%ba,exclude=aa%%aa-1")
    val expected = Right(Dependency("org", "name", "1.2").copy(exclude = CovariantSet(Module("fu", "ba"), ScalaModule("aa", "aa-1"))))
    expect(res == expected)
  }

  test("param") {
    val res = DependencyParser.parse("org:name:1.2,something=ba")
    val expected = Right(Dependency("org", "name", "1.2").copy(userParams = Map("something" -> Some("ba"))))
    expect(res == expected)
  }

  test("no-value param") {
    val res = DependencyParser.parse("org:name:1.2,something")
    val expected = Right(Dependency("org", "name", "1.2").copy(userParams = Map("something" -> None)))
    expect(res == expected)
  }

  test("scala + params + exclusions") {
    val res = DependencyParser.parse("org:::name::1.2,intransitive,exclude=foo%*,exclude=comp%%*,url=aaaa")
    val expected = Right(
      ScalaDependency(ScalaModule("org", "name", fullCrossVersion = true, platform = true), "1.2")
        .copy(
          exclude = CovariantSet(
            Module("foo", "*"),
            ScalaModule("comp", "*")
          ),
          userParams = Map(
            "intransitive" -> None,
            "url" -> Some("aaaa")
          )
        )
    )
    expect(res == expected)
  }
}
