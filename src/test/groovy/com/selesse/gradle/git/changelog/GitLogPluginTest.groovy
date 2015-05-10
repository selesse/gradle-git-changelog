package com.selesse.gradle.git.changelog
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class GitLogPluginTest {
    @Test
    public void testSomething() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'

        assertTrue(project.tasks.generateChangelog instanceof Task)
    }
}
