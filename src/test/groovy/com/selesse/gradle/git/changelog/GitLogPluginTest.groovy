package com.selesse.gradle.git.changelog

import com.google.common.io.Files
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class GitLogPluginTest {
    @Test public void testGenerateChangelogTask_isProperInstance() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertThat(project.tasks.generateChangelog instanceof GenerateChangelogTask).isTrue()
    }

    @Test public void testAssemble_dependsOnGenerateChangelog() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'base'
        project.pluginManager.apply 'com.selesse.git.changelog'
        project.evaluate()

        def outputDirectory = project.extensions.changelog.outputDirectory

        assertThat(project.tasks.assemble.dependsOn).contains(project.tasks.generateChangelog)
        // TODO(AS): Fix outputDirectory being modified in @TaskAction
        assertThat(outputDirectory == null ||
                (outputDirectory.absolutePath.replace("\\", "/") as String).endsWith('build')).isTrue()
    }

    @Test public void testProcessResources_dependsOnGenerateChangelog() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        project.pluginManager.apply 'java'
        project.evaluate()

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(getOutputDirectoryPath(project)).endsWith('build/resources/main')

        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        project.pluginManager.apply 'groovy'
        project.evaluate()

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(getOutputDirectoryPath(project)).endsWith('build/resources/main')

        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        project.pluginManager.apply 'war'
        project.evaluate()

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(getOutputDirectoryPath(project)).endsWith('build/resources/main')
    }

    @Test public void testSettingOutputDirectory_appliesToJavaPlugin() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        project.pluginManager.apply 'java'

        def arbitraryOutputDirectory = Files.createTempDir()
        project.extensions.changelog.outputDirectory = arbitraryOutputDirectory
        project.evaluate()

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(project.extensions.changelog.outputDirectory).isEqualTo(arbitraryOutputDirectory)
    }

    private String getOutputDirectoryPath(Project project) {
        project.extensions.changelog.outputDirectory.absolutePath.replace("\\", "/") as String
    }
}
