package com.selesse.gradle.git.changelog

import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin

class GitLogPlugin implements Plugin<Project> {
    Logger logger = Logging.getLogger(GitLogPlugin)
    private GitChangelogExtension extension
    private File defaultOutputDirectory

    @Override
    void apply(Project project) {
        defaultOutputDirectory = project.buildDir

        applyPluginDependency(project)

        extension = project.extensions.create("changelog", GitChangelogExtension)

        extension.with {
            title = project.name
            outputDirectory = defaultOutputDirectory
            fileName = 'CHANGELOG.md'
            commitFormat = '%ad%x09%s (%an)'
            since = 'beginning'
        }

        logger.info("Initialized with settings: ${extension}")
    }

    def applyPluginDependency(Project project) {
        GenerateChangelogTask task = project.tasks.create("generateChangelog", GenerateChangelogTask)

        if (project.tasks.findByName('processResources') != null) {
            Task processResources = project.tasks.processResources
            if (processResources != null) {
                logger.debug("Making processResources task depend on ${task.name}")
                processResources.dependsOn(task)

                defaultOutputDirectory = processResources.destinationDir
            }
        } else if (project.plugins.hasPlugin(BasePlugin)) {
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
