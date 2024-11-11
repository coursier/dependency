package dependency

import com.eed3si9n.expecty.Expecty.expect

class ToStringTests extends munit.FunSuite {

  test("simple module") {
    val mod = Module("org", "name")
    val str = mod.render
    val expected = "org:name"
    expect(str == expected)
  }

  test("scala module") {
    val mod = ScalaModule("org", "name")
    val str = mod.render
    val expected = "org::name"
    expect(str == expected)
  }

  test("scala module platform") {
    val mod = ScalaModule("org", "name", fullCrossVersion = false, platform = true)
    val str = mod.render
    val expected = "org::name:"
    expect(str == expected)
  }

  test("scala module full cross-version") {
    val mod = ScalaModule("org", "name", fullCrossVersion = true)
    val str = mod.render
    val expected = "org:::name"
    expect(str == expected)
  }

  test("simple dependency") {
    val dep = Dependency("org", "name", "1.2")
    val str = dep.render
    val expected = "org:name:1.2"
    expect(str == expected)
  }

  test("scala dependency") {
    val dep = ScalaDependency("org", "name", "1.2")
    val str = dep.render
    val expected = "org::name:1.2"
    expect(str == expected)
  }

  test("dependency with excludes") {
    val dep = Dependency("org", "name", "1.2").copy(
      exclude = CovariantSet(ScalaModule("fu", "ba"), Module("aa", "*"))
    )
    val str = dep.render
    val expected = "org:name:1.2,exclude=aa%*,exclude=fu%%ba"
    expect(str == expected)
  }

  test("dependency with params") {
    val dep = Dependency("org", "name", "1.2").copy(
      userParams = Seq(
        "intransitive" -> None,
        "aa" -> Some("bb")
      )
    )
    val str = dep.render
    val expected = "org:name:1.2,aa=bb,intransitive"
    expect(str == expected)
  }

}
