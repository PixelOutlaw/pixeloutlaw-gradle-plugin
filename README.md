# pixeloutlaw-gradle-plugin

> Provides standard defaults for PixelOutlaw Gradle-based projects and other useful plugins.

## Prerequisites

This plugin only works with versions of Gradle >= 7.1.1.

## Gradle Projects

```kotlin
plugins {
    id("io.pixeloutlaw.gradle") version "x.y.z"
}
```

### What It Does

* Adds publishing to Maven Central
* Adds generation of source and javadoc jars
* Sets Java compilation to target JDK 8
* Sets up Google Java Format for Java source files
* Sets Kotlin compilation to target JDK 8
* Adds Dokka generation for Kotlin files and adds it to javadoc jar
* Adds Detekt for Kotlin files
* Adds compileOnly dependencies to Maven pom as `provided` scope

## Spigot Projects

```kotlin
plugins {
    id("io.pixeloutlaw.spigot.build") version "x.y.z"
}
```

### What It Does

Adds a task (`runSpigotBuildTools`) that will download the Spigot BuildTools.jar and use it to install
Spigot in your `mavenLocal()` repository.

### How to Use It

Configure the `spigotBuildTools` extension in your build.gradle(.kts) to pass a list of versions that you
want to install.

You can also configure the plugin to skip building versions that already have a JAR in your Maven Local. This defaults
to `true`.

```kotlin
spigotBuildTools {
    skipExistingVersions = true
    versions = listOf("1.16.4")
}
```
```groovy
spigotBuildTools {
    skipExistingVersions = true
    versions = ["1.16.4"]
}
```
