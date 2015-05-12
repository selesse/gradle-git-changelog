package com.selesse.gradle.git.changelog
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

        assertThat(project.tasks.assemble.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(project.extensions.changelog.outputDirectory.absolutePath as String).endsWith('build')
    }

    @Test public void testProcessResources_dependsOnGenerateChangelog() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'java'
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(project.extensions.changelog.outputDirectory.absolutePath as String).endsWith('build/resources/main')

        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'groovy'
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(project.extensions.changelog.outputDirectory.absolutePath as String).endsWith('build/resources/main')

        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'war'
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertThat(project.tasks.processResources.dependsOn).contains(project.tasks.generateChangelog)
        assertThat(project.extensions.changelog.outputDirectory.absolutePath as String).endsWith('build/resources/main')
    }
}
