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
      exclude = Set(ScalaModule("com.ba", "fu"), Module("com.ba", "api"))
    )
    val res = scalaDep.applyParams(ScalaParameters("2.13.5", "2.13"))
    val expected = Dependency("org", "name_2.13", "1.2").copy(
      exclude = Set(Module("com.ba", "fu_2.13"), Module("com.ba", "api"))
    )
    expect(res == expected)
  }
}
