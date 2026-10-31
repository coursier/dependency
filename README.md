# dependency

[![Build status](https://github.com/coursier/dependency/workflows/CI/badge.svg)](https://github.com/coursier/dependency/actions?query=workflow%3ACI)
[![Maven Central](https://img.shields.io/maven-central/v/io.get-coursier/dependency_3.svg)](https://maven-badges.herokuapp.com/maven-central/io.get-coursier/dependency_3)

*dependency* is a library to parse and handle Java and Scala dependencies. It features:
- support for both Java and Scala dependencies (simply or fully cross-versioned, Scala.JS and Scala Native dependencies)
- support for exclusions and custom parameters (classifiers, override URLs for artifacts, …)
- support for Ivy module attributes
- `toString` representation that can be parsed back
- module matchers, accepting `*` wildcards
- powerful string interpolator
- Scala binary version computation
- …

## Usage

Add `io.get-coursier::dependency::0.3.3` to your build:
```scala
// Mill
def mvnDeps = Seq(mvn"io.get-coursier::dependency::0.3.3")
// sbt
libraryDependencies += "io.get-coursier" %%% "dependency" % "0.3.3"
```

The latest version is [![Maven Central](https://img.shields.io/maven-central/v/io.get-coursier/dependency_3.svg)](https://maven-badges.herokuapp.com/maven-central/io.get-coursier/dependency_3).

The examples below assume `dependency._` is imported:
```scala mdoc
import dependency._
```

*dependency* is published for Scala 2.12, 2.13, and 3 for the JVM, and for Scala 3 only for Scala.JS and Scala Native.
It is compatible with JDK back to 8.

### String syntax

The parsers and string interpolators below all accept the same syntax.

The separators before and after the name say how it should be cross-versioned:

| Dependency        | Kind                                | Name with Scala 2.13.6 and platform `sjs1` |
|-------------------|-------------------------------------|--------------------------------------------|
| `org:name:1.0`    | Java                                | `name`                                     |
| `org::name:1.0`   | Scala                               | `name_2.13`                                |
| `org:::name:1.0`  | Scala, fully cross-versioned        | `name_2.13.6`                              |
| `org::name::1.0`  | Scala, with platform                | `name_sjs1_2.13`                           |
| `org:::name::1.0` | Scala, fully cross-versioned, with platform | `name_sjs1_2.13.6`                 |

Modules are written the same way, without a version. As there's no version to
separate from the name, a platform suffix is then marked by a single trailing `:`, like in
`org::name:` or `org:::name:`.

Other parts of the syntax:

| Dependency                          | Meaning                                                              |
|-------------------------------------|----------------------------------------------------------------------|
| `org:name;attr1=a;attr2=b:1.0`      | Ivy module attributes, also accepted in modules                      |
| `org:name`                          | No version: `version` is empty                                       |
| `org:name:[1.0,2.0)`                | Version interval                                                     |
| `org:name:1.0:runtime`              | Inline configuration, added to `userParams` as `$inlineConfiguration` |
| `org:name:1.0,exclude=other%lib`    | Excludes the Java module `other:lib`                                 |
| `org:name:1.0,exclude=other%%lib`   | Excludes the Scala module `other::lib`                               |
| `org:name:1.0,key=value,flag`       | Custom parameters                                                    |

Organizations, names, and versions can't contain `/` or `\`.

`toString` gives back that syntax, so dependencies and modules can be parsed back:
```scala mdoc
val syntaxDep = dep"org::name::1.0,exclude=other%%lib,key=value"

assert(syntaxDep.toString == "org::name::1.0,exclude=other%%lib,key=value")
```

### Types

| Module        | Dependency        | Name attributes                                                   |
|---------------|-------------------|-------------------------------------------------------------------|
| `Module`      | `Dependency`      | None: the name is final                                           |
| `ScalaModule` | `ScalaDependency` | Scala ones: `applyParams` must be called to get the final name    |
| `AnyModule`   | `AnyDependency`   | Either of the above                                               |

`applyParams` returns a `Module` or a `Dependency`. Parsers return an `AnyModule` or an
`AnyDependency`, as the kind of their input is only known at runtime. String interpolators
return the most precise type:
```scala mdoc
val javaModule: Module = mod"org:name"
val scalaModule: ScalaModule = mod"org::name"
val javaDependency: Dependency = dep"org:name:1.0"
val scalaDependency: ScalaDependency = dep"org::name:1.0"
```

Exclusions are kept in a `CovariantSet`, a set that, unlike `Set`, is covariant in its element type.

### Dependencies

#### Parsing

```scala mdoc
import dependency.parser.DependencyParser

val input = "io.get-coursier::coursier:2.0.6"
val maybeDependency: Either[String, AnyDependency] = DependencyParser.parse(input)
val dep = maybeDependency.toOption.get
```

Parsing errors are returned as a `Left`, while invalid input passed to the string interpolators
below is a compile-time error:
```scala mdoc
assert(DependencyParser.parse("coursier") == Left("malformed module: coursier"))
assert(DependencyParser.parse("io/get-coursier:coursier:2.0.6") == Left("organization io/get-coursier contains invalid '/'"))
```

#### Converting to pure Java dependency

```scala mdoc
val params = ScalaParameters("2.13.6")
val javaDep: Dependency = dep.applyParams(params)

assert(javaDep.toString == "io.get-coursier:coursier_2.13:2.0.6")
```

#### Converting to pure Java dependency with a platform

```scala mdoc
val platformParams = ScalaParameters("2.13.6").copy(platform = Some("sjs1"))
val otherJavaDep: Dependency = dep"io.get-coursier::coursier::2.0.6".applyParams(platformParams)

assert(otherJavaDep.toString == "io.get-coursier:coursier_sjs1_2.13:2.0.6")
```

The platform, like `sjs1` for Scala.js 1.x or `native0.5` for Scala Native 0.5.x, is only added to
dependencies with a `::` before their version:
```scala mdoc
val noPlatformDep: Dependency = dep"io.get-coursier::coursier:2.0.6".applyParams(platformParams)

assert(noPlatformDep.toString == "io.get-coursier:coursier_2.13:2.0.6")
```

#### String interpolator

```scala mdoc
val dep1: AnyDependency = dep"io.get-coursier::coursier:2.0.6"

val depName = "coursier"
val ver = "2.0.6"
val dep2: AnyDependency = dep"io.get-coursier::$depName:$ver"
```

The interpolated strings are validated at compile-time. As a consequence, string parameters
(`depName`, `ver` above) should not contain separators (these will be part of the name, version, …,
else).

### Accessing fields

```scala mdoc
assert(dep.organization == "io.get-coursier")
assert(dep.name == "coursier")
assert(javaDep.name == "coursier_2.13")
assert(dep.version == "2.0.6")
```

### Modules

#### Parsing

```scala mdoc
import dependency.parser.ModuleParser

val moduleInput = "io.get-coursier::coursier"
val maybeModule: Either[String, AnyModule] = ModuleParser.parse(moduleInput)
val mod = maybeModule.toOption.get
```

#### Converting to pure Java module

```scala mdoc
val javaMod: Module = mod.applyParams(params)

assert(javaMod.toString == "io.get-coursier:coursier_2.13")
```

#### String interpolator

```scala mdoc
val mod1: AnyModule = mod"io.get-coursier::coursier"

val modName = "coursier"
val mod2: AnyModule = mod"io.get-coursier::$modName"
```

The interpolated strings are validated at compile-time. As a consequence, string parameters
(`modName` above) should not contain separators (these will be part of the name, …,
else).

### Accessing fields

```scala mdoc
assert(mod.organization == "io.get-coursier")
assert(mod.name == "coursier")
assert(javaMod.name == "coursier_2.13")
```

### Matching modules

`ModuleMatcher` matches modules against a module whose organization, name, and attribute values
can contain `*` wildcards:
```scala mdoc
val matcher = ModuleMatcher(mod"io.get-coursier::coursier-*")

assert(matcher.matches(mod"io.get-coursier::coursier-cache"))
assert(!matcher.matches(mod"io.get-coursier::dependency"))
```

Name attributes (Scala module or not, fully cross-versioned or not, platform suffix or not) are
taken into account too, unless `matchNameAttributes` is `false`:
```scala mdoc
assert(!matcher.matches(mod"io.get-coursier:coursier-cache"))

val looseMatcher = ModuleMatcher(mod"io.get-coursier:coursier-*", matchNameAttributes = false)
assert(looseMatcher.matches(mod"io.get-coursier:coursier-cache"))
assert(looseMatcher.matches(mod"io.get-coursier::coursier-cache"))
```

Matchers can also be parsed, and `ModuleMatcher.all` matches any module:
```scala mdoc
val parsedMatcher: Either[String, ModuleMatcher] = ModuleMatcher.parse("io.get-coursier::coursier-*")

assert(ModuleMatcher.all.matches(mod"io.get-coursier::coursier"))
```

### Exclusions

Exclusions can be specified in the string representations of dependencies:
```scala mdoc
val depWithExclusions = dep"io.get-coursier::coursier:2.0.6,exclude=io.argonaut%%argonaut,exclude=org.fusesource.jansi%jansi"

assert(depWithExclusions.exclude == CovariantSet(mod"io.argonaut::argonaut", mod"org.fusesource.jansi:jansi"))
```

(Note the use of `%` as a separator in excluded dependencies).

### Parameters

Custom parameters can be passed in dependencies:
```scala mdoc
val depWithParams = dep"io.get-coursier::coursier:2.0.6,url=https://dl.cs/cs.jar,intransitive"

assert(depWithParams.userParams == Seq("url" -> Some("https://dl.cs/cs.jar"), "intransitive" -> None))
```

*dependency* only keeps these parameters, it's up to users to give them a meaning. When
[converting to a coursier interface dependency](#to-coursier-interface-dependency), `classifier`,
`type`, `ext`, and `intransitive` are taken into account. Other parameters, like `url` above, are
ignored.

### Scala binary version

```scala mdoc
assert(ScalaVersion.binary("2.13.6") == "2.13")
assert(ScalaVersion.binary("3.0.0") == "3")
```

```scala mdoc
assert(ScalaVersion.jsBinary("1.5.1") == Some("1"))
assert(ScalaVersion.nativeBinary("0.4.0") == Some("0.4"))
```

### Conversions

#### To coursier interface dependency

Add `io.get-coursier::dependency-interface:0.3.3` to your build:
```scala
// Mill
def mvnDeps = Seq(mvn"io.get-coursier::dependency-interface:0.3.3")
// sbt
libraryDependencies += "io.get-coursier" %% "dependency-interface" % "0.3.3"
```

These conversions only accept `Module` and `Dependency`, so `applyParams` must be called first
on Scala modules and dependencies:
```scala mdoc
import dependency.api.ApiConverter
ApiConverter.module(mod"io.get-coursier::coursier".applyParams(ScalaParameters("2.13.6"))) // coursierapi.Module
ApiConverter.dependency(dep"io.get-coursier::coursier:2.0.6".applyParams(ScalaParameters("2.13.6"))) // coursierapi.Dependency
```

Alternatively, the dependency-interface module also provides implicit conversions for these:
```scala mdoc
import dependency.api.ops._
mod"io.get-coursier::coursier".applyParams(ScalaParameters("2.13.6")).toCs // coursierapi.Module
dep"io.get-coursier::coursier:2.0.6".applyParams(ScalaParameters("2.13.6")).toCs // coursierapi.Dependency
```

## Contributing

*dependency* is built with [Mill](https://mill-build.org). The `mill` launcher is in the repository,
and needs a JVM:
```text
$ ./mill -i __.test                       # run the tests
$ ./mill -i __.mimaReportBinaryIssues     # check binary compatibility
$ ./mill -i mdoc                          # compile the README examples
```

`mdoc` needs the [`cs` command](https://get-coursier.io/docs/cli-installation). It leaves
`README.md` as is, and only checks that the examples in `scala mdoc` code blocks compile and run.

Versions are deduced from `v*` git tags. Pushes to `main` publish snapshots, and pushing a tag
uploads a release to Sonatype Central, where it still has to be released manually.

## License

Licensed under the Apache 2.0 license.
