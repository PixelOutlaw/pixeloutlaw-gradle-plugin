package io.pixeloutlaw.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom

/**
 * Adds the MIT license to a generated POM.
 */
internal fun MavenPom.mitLicense() {
    licenses {
        license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
            distribution.set("repo")
        }
    }
}

/**
 * Adds any `compileOnly` Gradle dependencies to a generated POM as `provided` Maven dependency.
 */
internal fun MavenPom.addCompileOnlyDependenciesAsProvided(project: Project) {
    withXml {
        val root = asNode()
        // we only add compileOnly dependencies if the configuration even exists
        val dependencies = project.configurations.findByName("compileOnly")?.dependencies ?: return@withXml
        if (dependencies.size > 0) {
            val deps = root.children().find {
                it is groovy.util.Node && it.name().toString()
                    .endsWith("dependencies")
            } as groovy.util.Node? ?: root.appendNode("dependencies")
            dependencies.forEach { dependency ->
                deps.appendNode("dependency").apply {
                    appendNode("groupId", dependency.group)
                    appendNode("artifactId", dependency.name)
                    appendNode("version", dependency.version)
                    appendNode("scope", "provided")
                }
            }
        }
    }
}
