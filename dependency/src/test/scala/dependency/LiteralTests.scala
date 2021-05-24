package dependency

import com.eed3si9n.expecty.Expecty.expect

class LiteralTests extends munit.FunSuite {

  test("module") {
    val mod = mod"org:name"
    val expected = Module("org", "name")
    expect(mod == expected)
  }

  test("organization interpolation") {
    val theOrg = "orgg"
    val mod = mod"$theOrg:name"
    val expected = Module("orgg", "name")
    expect(mod == expected)
  }

  test("name interpolation") {
    val theName = "name"
    val mod = mod"org:$theName"
    val expected = Module("org", "name")
    expect(mod == expected)
  }

  test("scala name interpolation") {
    val theName = "name"
    val mod = mod"org::$theName"
    val expected = ScalaModule("org", "name")
    expect(mod == expected)
  }

  test("name mixed literal and interpolation") {
    val theName = "Name"
    val mod = mod"org:nameIs$theName"
    val expected = Module("org", "nameIsName")
    expect(mod == expected)
  }

  test("scala module") {
    val mod = mod"org::name"
    val expected = ScalaModule("org", "name")
    expect(mod == expected)
  }

  test("scala module full cross-version") {
    val mod = mod"org:::name"
    val expected = ScalaModule("org", "name", fullCrossVersion = true)
    expect(mod == expected)
  }

  test("dependency") {
    val mod = dep"org:name:1.2"
    val expected = Dependency("org", "name", "1.2")
    expect(mod == expected)
  }

  test("scala dependency") {
    val mod = dep"org::name:1.2"
    val expected = ScalaDependency("org", "name", "1.2")
    expect(mod == expected)
  }

  test("scala dependency full cross-version") {
    val mod = dep"org:::name:1.2"
    val expected = ScalaDependency(ScalaModule("org", "name", fullCrossVersion = true), "1.2")
    expect(mod == expected)
  }

  test("exclude interpolation") {
    val exclName = "ba"
    val dep = dep"org:name:1.2,exclude=fu%$exclName"
    val expected = Dependency("org", "name", "1.2").copy(
      exclude = Set(Module("fu", "ba"))
    )
  }

}
