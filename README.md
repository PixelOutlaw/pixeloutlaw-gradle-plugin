# pixeloutlaw-gradle-plugin

> Provides standard defaults for PixelOutlaw Gradle-based projects.

## What It Does

* Adds publishing to Bintray
* Adds generation of source and javadoc jars
* Sets Java compilation to target JDK 8
* Sets up Google Java Format for Java source files
* Sets Kotlin compilation to target JDK 8
* Adds Dokka generation for Kotlin files and adds it to javadoc jar
* Adds Detekt for Kotlin files
* Adds compileOnly dependencies to Maven pom as `provided` scope

## Installation

Edit the build.gradle(.kts) of the project you want to use the standards for.

### Single Module Projects

```kotlin
plugins {
    id("io.pixeloutlaw.single") version "x.y.z"
}
```

### Multiple Module Projects

```kotlin
plugins {
    id("io.pixeloutlaw.multi") version "x.y.z"
}
```
