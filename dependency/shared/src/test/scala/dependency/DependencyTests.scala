package dependency

import com.eed3si9n.expecty.Expecty.expect

class DependencyTests extends munit.FunSuite {

  test("simple") {
    val scalaDep = ScalaDependency("org", "name", "1.2")
    val res = scalaDep.applyParams(ScalaParameters("2.13.5", "2.13"))
    val expected = Dependency("org", "name_2.13", "1.2")
    expect(res == expected)
  }

  test("exclusions") {
    val scalaDep = ScalaDependency("org", "name", "1.2").copy(
      exclude = CovariantSet(ScalaModule("com.ba", "fu"), Module("com.ba", "api"))
    )
    val res = scalaDep.applyParams(ScalaParameters("2.13.5", "2.13"))
    val expected = Dependency("org", "name_2.13", "1.2").copy(
      exclude = CovariantSet(Module("com.ba", "fu_2.13"), Module("com.ba", "api"))
    )
    expect(res == expected)
  }

  test("refuse to instantiate a dependency with slash in org") {
    val createdModule =
      try {
        Dependency("or/g", "name", "1.2")
        true
      }
      catch {
        case _: IllegalArgumentException =>
          false
      }
    assert(!createdModule)
  }
  test("refuse to instantiate a dependency with backslash in org") {
    val createdModule =
      try {
        Dependency("org\\", "name", "1.2")
        true
      }
      catch {
        case _: IllegalArgumentException =>
          false
      }
    assert(!createdModule)
  }
  test("refuse to instantiate a dependency with slash in name") {
    val createdModule =
      try {
        Dependency("org", "n/ame", "1.2")
        true
      }
      catch {
        case _: IllegalArgumentException =>
          false
      }
    assert(!createdModule)
  }
  test("refuse to instantiate a dependency with backslash in name") {
    val createdModule =
      try {
        Dependency("org", "nam\\e", "1.2")
        true
      }
      catch {
        case _: IllegalArgumentException =>
          false
      }
    assert(!createdModule)
  }
  test("refuse to instantiate a dependency with slash in version") {
    val createdModule =
      try {
        Dependency("org", "name", "1./2")
        true
      }
      catch {
        case _: IllegalArgumentException =>
          false
      }
    assert(!createdModule)
  }
  test("refuse to instantiate a dependency with backslash in version") {
    val createdModule =
      try {
        Dependency("org", "name", "1\\.2")
        true
      }
      catch {
        case _: IllegalArgumentException =>
          false
      }
    assert(!createdModule)
  }
}
