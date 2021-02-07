package io.pixeloutlaw.gradle

import org.gradle.api.Project

class PixelOutlawSingleModuleGradlePlugin : PixelOutlawBaseModuleGradlePlugin() {
    override fun apply(target: Project) {
        // don't do anything if we aren't the root project
        if (target != target.rootProject) {
            return
        }

        super.apply(target)

        applyJavaConfiguration(target)
        applyKotlinConfiguration(target)
        applyMavenPublishConfiguration(target)
        applyNebulaMavenPublishConfiguration(target)
    }
}
