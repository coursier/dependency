package dependency.api

import com.eed3si9n.expecty.Expecty.expect
import dependency._
import dependency.api.ops._

class ApiTests extends munit.FunSuite {

  test("simple") {
    val scalaParams = ScalaParameters("2.13.12")
    val dep = dep"io.get-coursier::coursier:2.1.0"
    val csDep = dep.applyParams(scalaParams).toCs
    val expectedCsDep = coursierapi.Dependency.of("io.get-coursier", "coursier_2.13", "2.1.0")
    expect(csDep == expectedCsDep)
  }

  test("intransitive") {
    val scalaParams = ScalaParameters("2.13.12")
    val dep = dep"io.get-coursier::coursier:2.1.0,intransitive"
    val csDep = dep.applyParams(scalaParams).toCs
    val expectedCsDep = coursierapi.Dependency.of("io.get-coursier", "coursier_2.13", "2.1.0")
      .withTransitive(false)
    expect(csDep == expectedCsDep)
  }

  test("classifier") {
    val scalaParams = ScalaParameters("2.13.12")
    val dep = dep"io.get-coursier::coursier:2.1.0,classifier=tests"
    val csDep = dep.applyParams(scalaParams).toCs
    val expectedCsDep = coursierapi.Dependency.of("io.get-coursier", "coursier_2.13", "2.1.0")
      // .withClassifier("tests") // use this instead when we can benefit from coursier/interface#323
      .withPublication(new coursierapi.Publication("", "", "", "tests"))
    expect(csDep == expectedCsDep)
  }

  test("type") {
    val scalaParams = ScalaParameters("2.13.12")
    val dep = dep"io.get-coursier::coursier:2.1.0,type=jar"
    val csDep = dep.applyParams(scalaParams).toCs
    val expectedCsDep = coursierapi.Dependency.of("io.get-coursier", "coursier_2.13", "2.1.0")
      // .withType("jar") // use this instead when we can benefit from coursier/interface#323
      .withPublication(new coursierapi.Publication("", "jar", "", ""))
    expect(csDep == expectedCsDep)
  }

  test("ext") {
    val scalaParams = ScalaParameters("2.13.12")
    val dep = dep"io.get-coursier::coursier:2.1.0,ext=foo"
    val csDep = dep.applyParams(scalaParams).toCs
    val expectedCsDep = coursierapi.Dependency.of("io.get-coursier", "coursier_2.13", "2.1.0")
      .withPublication(new coursierapi.Publication("", "", "foo", ""))
    expect(csDep == expectedCsDep)
  }

  test("exclusions") {
    val scalaParams = ScalaParameters("2.13.12")
    val dep = dep"io.get-coursier::coursier:2.1.0,exclude=org.codehaus.plexus%plexus-container-default"
    val csDep = dep.applyParams(scalaParams).toCs
    val expectedCsDep = coursierapi.Dependency.of("io.get-coursier", "coursier_2.13", "2.1.0")
      .addExclusion("org.codehaus.plexus", "plexus-container-default")
    expect(csDep == expectedCsDep)
  }

}
