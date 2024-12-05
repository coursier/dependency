package dependency
package parser

import com.eed3si9n.expecty.Expecty.expect

class DependencyParserNoVersionTests extends munit.FunSuite {

  test("simple") {
    val res = DependencyParser.parse("org:name")
    val expected = Right(Dependency("org", "name", ""))
    expect(res == expected)
  }

  test("simple with colon") {
    val res = DependencyParser.parse("org:name:")
    val expected = Right(Dependency("org", "name", ""))
    expect(res == expected)
  }

  test("scala") {
    val res = DependencyParser.parse("org::name")
    val expected = Right(ScalaDependency("org", "name", ""))
    expect(res == expected)
  }

  test("scala full cross-version") {
    val res = DependencyParser.parse("org:::name")
    val expected = Right(ScalaDependency(ScalaModule("org", "name", fullCrossVersion = true), ""))
    expect(res == expected)
  }

  test("scala platform") {
    val res = DependencyParser.parse("org::name::")
    val expected = Right(ScalaDependency(ScalaModule("org", "name", fullCrossVersion = false, platform = true), ""))
    expect(res == expected)
  }

  test("scala with attributes") {
    val res = DependencyParser.parse("org::name;scala=2.12;sbt=1.0")
    val expected = Right(ScalaDependency(ScalaModule("org", "name").copy(attributes = Map("scala" -> "2.12", "sbt" -> "1.0")), ""))
    expect(res == expected)
  }

  test("attributes") {
    val res = DependencyParser.parse("org:name;scala=2.12;sbt=1.0")
    val expected = Right(Dependency(Module("org", "name").copy(attributes = Map("scala" -> "2.12", "sbt" -> "1.0")), ""))
    expect(res == expected)
  }

  test("exclude") {
    val res = DependencyParser.parse("org:name,exclude=fu%ba")
    val expected = Right(Dependency("org", "name", "").copy(exclude = CovariantSet(Module("fu", "ba"))))
    expect(res == expected)
  }

  test("scala exclude") {
    val res = DependencyParser.parse("org:name,exclude=fu%%ba")
    val expected = Right(Dependency("org", "name", "").copy(exclude = CovariantSet(ScalaModule("fu", "ba"))))
    expect(res == expected)
  }

  test("several exclude") {
    val res = DependencyParser.parse("org:name,exclude=fu%ba,exclude=aa%%aa-1")
    val expected = Right(Dependency("org", "name", "").copy(exclude = CovariantSet(Module("fu", "ba"), ScalaModule("aa", "aa-1"))))
    expect(res == expected)
  }

  test("param") {
    val res = DependencyParser.parse("org:name,something=ba")
    val expected = Right(Dependency("org", "name", "").copy(userParams = Seq("something" -> Some("ba"))))
    expect(res == expected)
  }

  test("no-value param") {
    val res = DependencyParser.parse("org:name,something")
    val expected = Right(Dependency("org", "name", "").copy(userParams = Seq("something" -> None)))
    expect(res == expected)
  }

  test("multiple same key params") {
    val res = DependencyParser.parse("org:name,something=a,something,something=b")
    val expected = Right(Dependency("org", "name", "").copy(
      userParams = Seq(
        "something" -> Some("a"),
        "something" -> None,
        "something" -> Some("b")
      )
    ))
    expect(res == expected)
  }

  test("scala + params + exclusions") {
    val res = DependencyParser.parse("org:::name::,intransitive,exclude=foo%*,exclude=comp%%*,url=aaaa")
    val expected = Right(
      ScalaDependency(ScalaModule("org", "name", fullCrossVersion = true, platform = true), "")
        .copy(
          exclude = CovariantSet(
            Module("foo", "*"),
            ScalaModule("comp", "*")
          ),
          userParams = Seq(
            "intransitive" -> None,
            "url" -> Some("aaaa")
          )
        )
    )
    expect(res == expected)
  }

  test("inline config") {
    val res = DependencyParser.parse("org:name: :runtime")
    val expected = Right(
      Dependency("org", "name", "").copy(
        userParams = Seq(
          "$inlineConfiguration" -> Some("runtime")
        )
      )
    )
    expect(res == expected)
  }
  test("inline config with param") {
    val res = DependencyParser.parse("org:name: :runtime,something=ba")
    val expected = Right(
      Dependency("org", "name", "").copy(
        userParams = Seq(
          "something" -> Some("ba"),
          "$inlineConfiguration" -> Some("runtime")
        )
      )
    )
    expect(res == expected)
  }

  test("reject slash in org") {
    val res = DependencyParser.parse("o/rg::name")
    expect(res.isLeft)
  }
  test("reject backslash in org") {
    val res = DependencyParser.parse("o\\rg::name")
    expect(res.isLeft)
  }
  test("reject slash in name") {
    val res = DependencyParser.parse("org::/name")
    expect(res.isLeft)
  }
  test("reject backslash in name") {
    val res = DependencyParser.parse("org::\\name")
    expect(res.isLeft)
  }
}
