package com.selesse.gradle.git.changelog

import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin

class GitLogPlugin implements Plugin<Project> {
    Logger logger = Logging.getLogger(GitLogPlugin)

    @Override
    void apply(Project project) {
        GitChangelogExtension extension = project.extensions.create("changelog", GitChangelogExtension, project)

        applyPluginDependency(project, extension)

        logger.info("Initialized with settings: ${extension}")
    }

    def applyPluginDependency(Project project, GitChangelogExtension extension) {
        GenerateChangelogTask task = project.tasks.create("generateChangelog", GenerateChangelogTask)
        task.extension = extension

        project.afterEvaluate {
            project.plugins.withType(JavaPlugin) {
                logger.info("Configuring Java plugin")
                Task processResources = project.tasks.processResources
                if (processResources != null) {
                    logger.debug("Making assembleTask depend on ${task.name}")
                    processResources.dependsOn(task)

                    logger.debug("Setting destination directory to {}", processResources.destinationDir)
                    task.extension.outputDirectory = processResources.destinationDir
                }
            }

            if (hasAndroidPlugin()) {
                project.plugins.withType(getAndroidPlugin()) {
                    logger.info("Configuring Android plugin")
                    project.android.applicationVariants.all { variant ->
                        def variantName = variant.name.capitalize()
                        def taskName = "generate${variantName}Changelog"
                        def newTask = project.tasks.create(taskName, GenerateChangelogTask)
                        newTask.extension = project.extensions.create("changelog${variantName}",
                                GitChangelogExtension, project)

                        File androidOutputDirectory = project.file(
                                "$project.buildDir/generated/source/changelog/${variant.dirName}"
                        )

                        newTask.extension.outputDirectory = androidOutputDirectory
                        variant.registerResGeneratingTask(newTask, androidOutputDirectory)
                    }
                }
            }

            if (project.plugins.findPlugin(JavaPlugin) == null && !hasAndroidPlugin()) {
                project.plugins.withType(BasePlugin) {
                    logger.info("Configuring Base Plugin")
                    Task assembleTask = project.tasks.getByName("assemble") as Task
                    if (assembleTask != null) {
                        logger.debug("Making assembleTask depend on ${task.name}")
                        assembleTask.dependsOn(task)
                    }
                }
            }
        }
    }

    static boolean hasAndroidPlugin() {
        return getAndroidPlugin() != null
    }

    static Class getAndroidPlugin() {
        def androidPluginClass
        def androidLibraryClass

        try {
            androidPluginClass = Class.forName('com.android.build.gradle.AppPlugin')
            androidLibraryClass = Class.forName('com.android.build.gradle.LibraryPlugin')
        } catch (ClassNotFoundException ignored) {
            androidPluginClass = null
            androidLibraryClass = null
        }

        if (androidPluginClass != null) {
            return androidPluginClass
        }
        if (androidLibraryClass != null) {
            return androidLibraryClass
        }
        return null
    }
}
