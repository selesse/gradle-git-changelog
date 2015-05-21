package com.selesse.gradle.git.changelog.generator
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.selesse.gradle.git.changelog.generator.BaseWriterTest.*
import static org.assertj.core.api.Assertions.assertThat

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

    @Test public void testDefaultTemplateIsUsed_withNoTags() {
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()
        temporaryGitDirectory = repository.getDirectory()

        String htmlChangelog = writeHtmlChangelog(createHtmlWriter(project, temporaryGitDirectory))

        def html = new XmlSlurper(false, false, true).parseText(htmlChangelog)

        assertThat(html.name()).isEqualTo('html')
        // There exists an <h1> with the project's title
        assertThat(html.body.h1.text()).isEqualTo(project.extensions.changelog.title)
        // There are *NO* h2s since there are no tags for this repository
        assertThat(html.body.div.h2.size()).isEqualTo(0)
        assertThat(html.body.div.li.size()).isEqualTo(3)

        html.body.div.li.each {
            assertThat(it.text()).isIn(
                    '[ci skip] Remove contributors (Test Account)',
                    'Add contributors (Test Account)',
                    'Initial commit from the past (Test Account)'
            )
        }
    }

    @Test public void testDefaultTemplateIsUsed_withTags() {
        GitRepositoryBuilder repository =
                createGitRepositoryBuilderRunner()
                    .runCommand('git tag v1.0')
                    .build()
        temporaryGitDirectory = repository.getDirectory()

        String htmlChangelog = writeHtmlChangelog(createHtmlWriter(project, temporaryGitDirectory))

        def html = new XmlSlurper(false, false, true).parseText(htmlChangelog)

        assertThat(html.name()).isEqualTo('html')
        assertThat(html.body.h1.text()).isEqualTo(project.extensions.changelog.title)
        assertThat(html.body.div.h2.size()).isEqualTo(1)
        assertThat(html.body.div.h2.text()).startsWith('v1.0')
        assertThat(html.body.div.li.size()).isEqualTo(3)

        html.body.div.li.each {
            assertThat(it.text()).isIn(
                    '[ci skip] Remove contributors (Test Account)',
                    'Add contributors (Test Account)',
                    'Initial commit from the past (Test Account)'
            )
        }
    }
}
