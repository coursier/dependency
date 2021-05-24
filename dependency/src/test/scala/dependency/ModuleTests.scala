package dependency

import com.eed3si9n.expecty.Expecty.expect

class ModuleTests extends munit.FunSuite {

  test("scala to java") {
    val scalaMod = ScalaModule("org", "name")
    val mod = scalaMod.applyParams(ScalaParameters("2.13.5", "2.13"))
    val expected = Module("org", "name_2.13")
    expect(mod == expected)
  }

  test("scala full cross-version to java") {
    val scalaMod = ScalaModule("org", "name", fullCrossVersion = true)
    val mod = scalaMod.applyParams(ScalaParameters("2.13.5", "2.13"))
    val expected = Module("org", "name_2.13.5")
    expect(mod == expected)
  }

  test("scala platform to java") {
    val scalaMod = ScalaModule("org", "name", fullCrossVersion = false, platform = true)
    val mod = scalaMod.applyParams(ScalaParameters("2.13.5", "2.13", Some("native0.4")))
    val expected = Module("org", "name_native0.4_2.13")
    expect(mod == expected)
  }

  test("scala full cross-version platform to java") {
    val scalaMod = ScalaModule("org", "name", fullCrossVersion = true, platform = true)
    val mod = scalaMod.applyParams(ScalaParameters("2.13.5", "2.13", Some("native0.4")))
    val expected = Module("org", "name_native0.4_2.13.5")
    expect(mod == expected)
  }

}
