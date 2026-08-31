package dependency

import com.eed3si9n.expecty.Expecty.expect

class ModuleMatcherTests extends munit.FunSuite {

  private def check(
    matcher: ModuleMatcher,
    shouldMatch: Seq[AnyModule] = Nil,
    shouldNotMatch: Seq[AnyModule] = Nil
  ): Unit = {
    for (mod <- shouldMatch)
      assert(matcher.matches(mod), s"$matcher should match $mod")
    for (mod <- shouldNotMatch)
      assert(!matcher.matches(mod), s"$matcher should not match $mod")
  }

  test("name wildcard") {
    check(
      ModuleMatcher(mod"com.lihaoyi:os-*"),
      shouldMatch = Seq(
        mod"com.lihaoyi:os-lib_2.13",
        mod"com.lihaoyi:os-lib_3",
        mod"com.lihaoyi:os-"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:upickle_2.13",
        mod"com.lihaoyi:mill-os-lib_2.13",
        mod"com.example:os-lib_2.13"
      )
    )
  }

  test("organization wildcard") {
    check(
      ModuleMatcher(mod"com.*:pprint_2.13"),
      shouldMatch = Seq(
        mod"com.lihaoyi:pprint_2.13",
        mod"com.example:pprint_2.13"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:sourcecode_2.13",
        mod"org.scala-lang:pprint_2.13"
      )
    )
  }

  test("wildcard in the middle") {
    check(
      ModuleMatcher(mod"com.lihaoyi:mill-*_2.13"),
      shouldMatch = Seq(
        mod"com.lihaoyi:mill-main_2.13",
        mod"com.lihaoyi:mill-scalalib_2.13"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:mill-main_2.12",
        mod"com.lihaoyi:mill-main",
        mod"com.lihaoyii:mill-main_2.13"
      )
    )
  }

  test("several wildcards") {
    check(
      ModuleMatcher(mod"*:*-lib*"),
      shouldMatch = Seq(
        mod"com.lihaoyi:os-lib_2.13",
        mod"com.lihaoyi:os-lib",
        mod"com.example:some-lib_3"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:upickle_2.13",
        mod"com.lihaoyi:os-li"
      )
    )
  }

  test("no wildcard") {
    check(
      ModuleMatcher(mod"com.lihaoyi:os-lib"),
      shouldMatch = Seq(
        mod"com.lihaoyi:os-lib"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:os-lib_2.13",
        mod"com.lihaoyi:os"
      )
    )
  }

  test("all") {
    check(
      ModuleMatcher.all,
      shouldMatch = Seq(
        mod"com.lihaoyi:os-lib_2.13",
        mod"com.lihaoyi:os-lib",
        mod"com.lihaoyi::os-lib",
        mod"com.lihaoyi:::os-lib",
        mod"com.lihaoyi::scalatags:",
        ModuleLike("", "", NoAttributes, Map.empty),
        ModuleLike("com.lihaoyi", "", NoAttributes, Map.empty),
        ModuleLike("", "os-lib", NoAttributes, Map.empty)
      )
    )
  }

  test("scala module") {
    check(
      ModuleMatcher(mod"com.lihaoyi::u*"),
      shouldMatch = Seq(
        mod"com.lihaoyi::upickle",
        mod"com.lihaoyi::ujson"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:upickle",
        mod"com.lihaoyi:::upickle",
        mod"com.lihaoyi::upickle:",
        mod"com.example::upickle"
      )
    )
  }

  test("scala module, full cross version and platform") {
    check(
      ModuleMatcher(mod"com.lihaoyi:::scalatags:"),
      shouldMatch = Seq(
        mod"com.lihaoyi:::scalatags:"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:::scalatags",
        mod"com.lihaoyi::scalatags:",
        mod"com.lihaoyi::scalatags"
      )
    )
  }

  test("ignoring name attributes") {
    check(
      ModuleMatcher(mod"com.lihaoyi:os-*", matchNameAttributes = false),
      shouldMatch = Seq(
        mod"com.lihaoyi:os-lib",
        mod"com.lihaoyi::os-lib",
        mod"com.lihaoyi:::os-lib:"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi::upickle"
      )
    )
  }

  test("attributes") {
    check(
      ModuleMatcher(mod"com.lihaoyi:os-lib;classifier=*"),
      shouldMatch = Seq(
        mod"com.lihaoyi:os-lib;classifier=tests",
        mod"com.lihaoyi:os-lib;classifier=sources"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:os-lib",
        mod"com.lihaoyi:os-lib;type=jar",
        mod"com.lihaoyi:os-lib;classifier=tests;type=jar"
      )
    )
  }

  test("parse") {
    val matcher = ModuleMatcher.parse("com.lihaoyi::os-*").toOption.get
    check(
      matcher,
      shouldMatch = Seq(
        mod"com.lihaoyi::os-lib"
      ),
      shouldNotMatch = Seq(
        mod"com.lihaoyi:os-lib"
      )
    )
  }

  test("parse error") {
    expect(ModuleMatcher.parse("com.lihaoyi").isLeft)
  }

  test("blob matches") {
    expect(ModuleMatcher.blobMatches("*", ""))
    expect(ModuleMatcher.blobMatches("**", "os-lib"))
    expect(ModuleMatcher.blobMatches("", ""))
    expect(!ModuleMatcher.blobMatches("", "os-lib"))
    expect(ModuleMatcher.blobMatches("a*a*a", "aaa"))
    expect(!ModuleMatcher.blobMatches("a*a*a", "aa"))
    expect(ModuleMatcher.blobMatches("*aa*aa", "aaaa"))
    expect(!ModuleMatcher.blobMatches("*aa*aa", "aaa"))
  }
}
