package dependency

import com.eed3si9n.expecty.Expecty.expect

class TypedLiteralTests extends munit.FunSuite {

  test("typed module") {
    val mod: Module = mod"org:name"
    val expected = Module("org", "name")
    expect(mod == expected)
  }

  test("typed scala module") {
    val mod: ScalaModule = mod"org::name"
    val expected = ScalaModule("org", "name")
    expect(mod == expected)
  }

  test("typed dependency") {
    val mod: Dependency = dep"org:name:1.2"
    val expected = Dependency("org", "name", "1.2")
    expect(mod == expected)
  }

  test("typed scala dependency") {
    val mod: ScalaDependency = dep"org::name:1.2"
    val expected = ScalaDependency("org", "name", "1.2")
    expect(mod == expected)
  }

  test("typed dependency with excludes") {
    val mod: Dependency = dep"org:name:1.2,exclude=aa%bb,exclude=aa1%bb1"
    val expected = Dependency("org", "name", "1.2").copy(
      exclude = CovariantSet(Module("aa", "bb"), Module("aa1", "bb1"))
    )
    expect(mod == expected)
  }

  test("typed scala dependency with excludes") {
    val mod: ScalaDependency = dep"org::name:1.2,exclude=aa%bb,exclude=aa1%bb1"
    val expected = ScalaDependency("org", "name", "1.2").copy(
      exclude = CovariantSet(Module("aa", "bb"), Module("aa1", "bb1"))
    )
    expect(mod == expected)
  }

  test("typed scala dependency with scala excludes") {
    val mod: ScalaDependency = dep"org::name:1.2,exclude=aa%%bb,exclude=aa1%%bb1"
    val expected = ScalaDependency("org", "name", "1.2").copy(
      exclude = CovariantSet(ScalaModule("aa", "bb"), ScalaModule("aa1", "bb1"))
    )
    expect(mod == expected)
  }

  test("typed scala dependency with mixed java / scala excludes") {
    val mod: ScalaDependency = dep"org::name:1.2,exclude=aa%bb,exclude=aa1%%bb1"
    val expected = ScalaDependency("org", "name", "1.2").copy(
      exclude = CovariantSet(Module("aa", "bb"), ScalaModule("aa1", "bb1"))
    )
    expect(mod == expected)
  }

}
