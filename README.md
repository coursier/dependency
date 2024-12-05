# dependency

[![Build status](https://github.com/coursier/dependency/workflows/CI/badge.svg)](https://github.com/coursier/dependency/actions?query=workflow%3ACI)
[![Maven Central](https://img.shields.io/maven-central/v/io.get-coursier/dependency_3.svg)](https://maven-badges.herokuapp.com/maven-central/io.get-coursier/dependency_3)

*dependency* is a library to parse and handle Java and Scala dependencies. It features:
- support for both Java and Scala dependencies (simply or fully cross-versioned, Scala.JS and Scala Native dependencies)
- support for exclusions, override URLs for artifacts, …
- support for Ivy module attributes
- `toString` representation that can be parsed back
- powerful string interpolator
- Scala binary version computation
- …

## Usage

Add `io.get-coursier::dependency::0.3.2` to your build:
```scala
// Mill
def ivyDeps = Agg(ivy"io.get-coursier::dependency::0.3.2")
// sbt
libraryDependencies += "io.get-coursier" %%% "dependency" % "0.3.2"
```

The latest version is [![Maven Central](https://img.shields.io/maven-central/v/io.get-coursier/dependency_3.svg)](https://maven-badges.herokuapp.com/maven-central/io.get-coursier/dependency_3).

The examples below assume `dependency._` is imported:
```scala mdoc
import dependency._
```

*dependency* is published for Scala 2.12, 2.13, and 3, for the JVM, Scala.JS, and Scala Native.
It is compatible with JDK back to 8.

### Dependencies

#### Parsing

```scala mdoc
import dependency.parser.DependencyParser

val input = "io.get-coursier::coursier:2.0.6"
val maybeDependency: Either[String, AnyDependency] = DependencyParser.parse(input)
val dep = maybeDependency.toOption.get
```

#### Converting to pure Java dependency

```scala mdoc
val params = ScalaParameters("2.13.6")
val javaDep: Dependency = dep.applyParams(params)

assert(javaDep.toString == "io.get-coursier:coursier_2.13:2.0.6")
```

#### Converting to pure Java dependency with a platform

```scala mdoc
val platformParams = ScalaParameters("2.13.6").copy(platform = Some("myplatform2"))
val otherJavaDep: Dependency = dep"io.get-coursier::coursier::2.0.6".applyParams(platformParams)

assert(otherJavaDep.toString == "io.get-coursier:coursier_myplatform2_2.13:2.0.6")
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

Add a dependency towards `io.get-coursier::dependency-interface`:

Add `io.get-coursier::dependency-interface:0.2.3` to your build:
```scala
// sbt
libraryDependencies += "io.get-coursier" %% "dependency-interface" % "0.2.3"
// mill
def ivyDeps = Agg(ivy"io.get-coursier::dependency-interface:0.2.3")
```

Then do
```scala
import dependency.api.ApiConverter
ApiConverter.module(mod"io.get-coursier::coursier") // coursierapi.Module
ApiConverter.dependency(dep"io.get-coursier::coursier:2.0.6".applyParams(ScalaParameters("2.13.6"))) // coursierapi.Dependency
```

Alternatively, the dependency-interface module also provides implicit conversions for these:
```scala
import dependency.api.ops._
mod"io.get-coursier::coursier".toCs // coursierapi.Module
dep"io.get-coursier::coursier:2.0.6".applyParams(ScalaParameters("2.13.6")).toCs // coursierapi.Dependency
```

## License

Licensed under the Apache 2.0 license.
