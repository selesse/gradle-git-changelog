package com.selesse.gradle.git.changelog.generator
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.selesse.gradle.git.changelog.generator.BaseWriterTest.*;

class HtmlChangelogWriterTest {
    Project project
    GenerateChangelogTask task
    File temporaryGitDirectory

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        task = project.tasks.generateChangelog as GenerateChangelogTask

        project.extensions.changelog.since = 'beginning'
    }

    @After
    public void cleanup() {
        if (temporaryGitDirectory != null && temporaryGitDirectory.isDirectory()) {
            temporaryGitDirectory.deleteDir()
        }
    }

    @Test public void testDefaultTemplateIsUsed() {
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()
        temporaryGitDirectory = repository.getDirectory()

        String changelogContent = writeHtmlChangelog(createHtmlWriter(project, temporaryGitDirectory))

        then:
        changelogContent.contains('<!DOCTYPE html>')
    }
}
