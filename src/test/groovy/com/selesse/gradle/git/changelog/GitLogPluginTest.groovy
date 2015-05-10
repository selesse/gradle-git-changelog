package com.selesse.gradle.git.changelog
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class GitLogPluginTest {
    @Test public void testGenerateChangelogTask_isProperInstance() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertTrue(project.tasks.generateChangelog instanceof GenerateChangelogTask)
    }

    @Test public void testAssemble_dependsOnGenerateChangelog() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'base'
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertTrue(project.tasks.assemble.dependsOn.contains(project.tasks.generateChangelog))
    }
}
