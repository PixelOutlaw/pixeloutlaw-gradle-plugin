package io.pixeloutlaw.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin

fun Project.withJavaPlugin(action: AppliedPlugin.() -> Unit) = pluginManager.withPlugin("java", action)

fun Project.withKotlinJvmPlugin(action: AppliedPlugin.() -> Unit) =
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm", action)

fun Project.withMavenPublish(action: AppliedPlugin.() -> Unit) = pluginManager.withPlugin("maven-publish", action)
