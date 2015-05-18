package com.selesse.gradle.git.changelog.tasks

import com.selesse.gitwrapper.fixtures.GitRepositoryBuilder
import com.selesse.gitwrapper.fixtures.GitRepositoryBuilderRunner
import com.selesse.gradle.git.GitCommandExecutor
import com.selesse.gradle.git.changelog.GitChangelogExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import spock.lang.Specification

class GenerateChangelogTaskTest extends Specification {
    File temporaryGitDirectory

    @After public void tearDown() {
        if (temporaryGitDirectory != null && temporaryGitDirectory.isDirectory()) {
            temporaryGitDirectory.deleteDir()
        }
    }

    def "title is included in changelog"() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        GenerateChangelogTask task = project.tasks.generateChangelog as GenerateChangelogTask

        when:
        def changelog = task.generateChangelog()

        then:
        changelog.contains(project.name)

        when:
        project.extensions.changelog.title = 'Non-default plugin title'
        def byteArrayOutputStream = new ByteArrayOutputStream()
        def stream = new PrintStream(byteArrayOutputStream)
        changelog = task.generateChangelog(stream)

        then:
        changelog.contains('Non-default plugin title')
        byteArrayOutputStream.toString('UTF-8') == changelog
    }

    def "filters out undesired lines"() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        GenerateChangelogTask task = project.tasks.generateChangelog as GenerateChangelogTask

        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()

        temporaryGitDirectory = repository.getDirectory()
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)

        GitChangelogExtension extension = new GitChangelogExtension()
        extension.includeLines = { !it.contains('[ci skip]') }

        String changelogContent = task.generateChangelogContent(extension, gitCommandExecutor)

        then:
        !changelogContent.contains('[ci skip]')
    }

    def "processes lines according to the extension property"() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.selesse.git.changelog'
        GenerateChangelogTask task = project.tasks.generateChangelog as GenerateChangelogTask

        when:
        GitRepositoryBuilder repository = createGitRepositoryBuilderRunner().build()

        temporaryGitDirectory = repository.getDirectory()
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor('%s (%an)', temporaryGitDirectory)

        GitChangelogExtension extension = new GitChangelogExtension()
        extension.processLines = {
            String input = it as String
            if (input.startsWith('[ci skip]')) {
                input = "Hi mom! => ${input.minus('[ci skip] ')}"
            }
            input
        }

        String changelogContent = task.generateChangelogContent(extension, gitCommandExecutor)

        then:
        changelogContent.contains('Hi mom! => Remove contributors')
    }

    private GitRepositoryBuilderRunner createGitRepositoryBuilderRunner() {
        GitRepositoryBuilder.create()
                .runCommand('git init')
                .runCommand('git', 'config', 'user.name', 'Test Account')
                .createFile('README.md', 'Hello world!')
                .runCommand('git add README.md')
                .runCommand('git', 'commit', '-m', 'Initial commit from the past')
                .runCommand('git', 'commit', '--amend', '--date=2015-05-01 00:00:00 -0400', '-C', 'HEAD')
                .createFile('CONTRIBUTORS.md', 'This is a list of contributors to this project')
                .runCommand('git add CONTRIBUTORS.md')
                .runCommand('git', 'commit', '-m', 'Add contributors')
                .runCommand('git rm CONTRIBUTORS.md')
                .runCommand('git', 'commit', '-m', '[ci skip] Remove contributors')
    }

}
