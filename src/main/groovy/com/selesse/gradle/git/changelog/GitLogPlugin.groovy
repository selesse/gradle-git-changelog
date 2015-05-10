package com.selesse.gradle.git.changelog
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin

class GitLogPlugin implements Plugin<Project> {
    Logger logger = Logging.getLogger(GitLogPlugin)
    private GitChangelogExtension extension

    @Override
    void apply(Project project) {
        extension = project.extensions.create("changelog", GitChangelogExtension)

        extension.with {
            title = project.name
        }
        GenerateChangelogTask task = project.tasks.create("generateChangelog", GenerateChangelogTask)

        if (project.plugins.hasPlugin(BasePlugin)) {
            DefaultTask assembleTask = project.tasks.getByName("assemble") as DefaultTask
            if (assembleTask != null) {
                logger.debug("Making assemble task depend on ${task.name}")
                assembleTask.dependsOn(task)
            }
        } else {
            logger.info('Base plugin could not be found, tasks will not be injected')
        }

    }
}
