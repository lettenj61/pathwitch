Pathwitch
=========

Tiny glob library for Scala

## Table of contents

* [Installation](#installtion)
* [Tutorial](#tutorial)
  * [Glob](#glob)
  * [GlobSet](#globset)
  * [Configuration](#configuration)
* [License](#license)

## About

This library provides minimal glob functions for Scala JVM / Scala.js.

Generally, you don't need extra glob tool on JVM, as it supports `PathMatcher`s in NIO/2 package since Java version 7, and Scala got great `better-files` which wraps it (and all other stuff from `java.nio.file`) and sanitizes its weirdo behaviours in Scala way.

For JavaScript environment, Scala.js doesn't support NIO/2 (yet!), but tons of battle-ready glob packages are available via NPM, like `fast-glob`, `node-glob`, `minimatch`, etc.

I made `pathwitch` mainly for use with Scala.js, just for the cases that I could not rely on NPM packages or so. Secondary, for my Scala studies.

## Installation

T.B.D.

## Tutorial

`pathwitch`'s main function are just 2 objects, `Glob` and `GlobSet`, with `GlobConfig` to customize their behaviour.

Let's assume we're playing with it in Scala REPL.

First things first, we need imports:

```scala
$ scala

Welcome to Scala 2.12.4 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_181).
Type in expressions for evaluation. Or try :help.

scala> import pathwitch.{Glob, GlobConfig, GlobSet}
import pathwitch.{Glob, GlobConfig, GlobSet}
```

### Glob

For those who are not familiar with word `glob`, this is the [document][glob-doc] which `pathwitch` follows as specification. (TODO: no link!)

In `pathwitch`, basically a `Glob` is merely a function of type `String => Boolean`.

Before using it, we have to decide which character to use as "path separator". Like code below:

```scala
scala> implicit val separator: Glob.Separator = Glob.Slash
separator: pathwitch.Glob.Separator = Slash
```

Now we can instantiate `Glob`s with its factory:

```scala
scala> val glob = Glob("*.scala")
glob: pathwitch.Glob = Glob(*.scala,[^/\\]*\.scala)
```

If we forget to define implicit `Separator`, instantiation will fail with:

```scala
scala> Glob("*.java")
<console>:15: error: could not find implicit value for parameter separator: pathwitch.Glob.Separator
       Glob("*.java")
           ^
```

For convenience, but only when on JVM, `pathwitch` provides implicit `Separator` instance which defaults to use `java.io.File.separatorChar`.

To use it:

```scala
import pathwitch.Implicits.pathSeparator
```

Note that this implicit object never exists on JS platform, as there may be no `File` class on runtime.

`pathwitch` converts glob pattern string into Java regular expression string, of type `java.util.regex.Pattern`, wrapped inside `scala.util.matching.Regex`.

```scala
scala> glob.regex
res1: scala.util.matching.Regex = [^/\\]*\.scala

scala> glob.regex.pattern
res2: java.util.regex.Pattern = [^/\\]*\.scala
```

A glob can be used to match `String`s.

```scala
scala> glob.matches("Predef.scala") // or simply glob("...")
res3: Boolean = true
```

As glob implements `scala.Function2[String, Boolean]`, you can pass it as predicate to any arbitary object:

```scala
scala> List("A.scala", "BC.scala", "D.java").filter(glob)
res4: List[String] = List(A.scala, BC.scala)
```

### GlobSet

GlobSet is a utility to use multiple patterns in matching strings.

By default, `matches` function of `GlobSet` returns `true` when any of its pattern matches to the string.

```scala

```

### Configuration

You can customize behaviour of a glob either in creation, or at matching.

Here is short example of how to configure globs with `GlobConfig`:

```scala
val config = GlobConfig(
  separator = Glob.Slash, // Should be either of Glob.Slash or Glob.Backslash
  unixStyle = false, // Replace all '\' character to '/' on matching
  prefixSlash = false, // Prepend '/' when pattern starts with glob stars
  suffixStar = false // Append glob star when pattern ends with '/'
)
```

## License

MIT.

---

[Back to TOC](#table-of-contents)

[glob-doc]: #