package io.pixeloutlaw.gradle

import org.gradle.api.Project

class PixelOutlawMultiModuleGradlePlugin : PixelOutlawBaseModuleGradlePlugin() {
    override fun apply(target: Project) {
        // don't do anything if we aren't the root project
        if (target != target.rootProject) {
            return
        }

        super.apply(target)

        target.subprojects {
            val subTarget = this@subprojects

            applyJavaConfiguration(subTarget)
            applyKotlinConfiguration(subTarget)
            applyMavenPublishConfiguration(subTarget)
        }
    }
}
